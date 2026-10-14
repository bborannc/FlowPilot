package com.enoca.flowpilot.service.impl;

import com.enoca.flowpilot.core.entities.ApprovalStep;
import com.enoca.flowpilot.core.entities.Employee;
import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.core.exceptions.BusinessRuleException;
import com.enoca.flowpilot.core.enums.ApprovalStepStatus;
import com.enoca.flowpilot.dto.request.ApproveRequestDto;
import com.enoca.flowpilot.dto.request.RejectRequestDto;
import com.enoca.flowpilot.repository.ApprovalHistoryRepository;
import com.enoca.flowpilot.repository.ApprovalStepRepository;
import com.enoca.flowpilot.repository.RequestRepository;
import com.enoca.flowpilot.repository.RoleRepository;
import com.enoca.flowpilot.security.ApprovalAuthorizationService;
import com.enoca.flowpilot.security.CurrentUserService;
import com.enoca.flowpilot.service.EmployeeService;
import com.enoca.flowpilot.core.entities.Role;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceImplTest {

    @Mock
    private ApprovalStepRepository stepRepository;

    @Mock
    private ApprovalHistoryRepository historyRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private ApprovalAuthorizationService approvalAuthorizationService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ApprovalServiceImpl approvalService;

    @Test
    void yetkisizCalisanOnayAdiminiOnaylayamamali() {
        ApprovalStep step = mock(ApprovalStep.class);

        when(stepRepository.findById(14L))
                .thenReturn(Optional.of(step));

        doThrow(new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Bu onay adımında işlem yapma yetkiniz yok."
        )).when(approvalAuthorizationService).assertCanActOnStep(step);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> approvalService.approveStep(
                        14L,
                        mock(ApproveRequestDto.class)
                )
        );

        assertEquals(403, exception.getStatusCode().value());

        // Yetki kontrolü başarısızsa adım kaydedilmemeli.
        verify(stepRepository, never()).save(step);
    }

    @Test
    void oncekiAdimTamamlanmadanSonrakiAdimOnaylanamamali() {
        ApprovalStep currentStep = mock(ApprovalStep.class);
        ApprovalStep previousStep = mock(ApprovalStep.class);
        Request request = mock(Request.class);

        when(stepRepository.findById(14L))
                .thenReturn(Optional.of(currentStep));

        when(currentStep.getStatus())
                .thenReturn(ApprovalStepStatus.PENDING);
        when(currentStep.getStepOrder()).thenReturn(2);
        when(currentStep.getRequest()).thenReturn(request);
        when(request.getId()).thenReturn(100L);

        when(previousStep.getStepOrder()).thenReturn(1);
        when(previousStep.getStatus())
                .thenReturn(ApprovalStepStatus.PENDING);

        when(stepRepository.findByRequestIdOrderByStepOrderAsc(100L))
                .thenReturn(List.of(previousStep, currentStep));

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> approvalService.approveStep(
                        14L,
                        mock(ApproveRequestDto.class)
                )
        );

        // Birinci adım hâlâ beklemede olduğundan ikinci adım reddedilmeli.
        verify(stepRepository, never()).save(currentStep);

        // Henüz bir onay işlemi gerçekleşmediğinden kullanıcı sorgulanmamalı.
        verify(currentUserService, never()).getCurrentEmployeeId();
    }

    @Test
    void zatenOnaylanmisAdimTekrarOnaylanamamali() {
        ApprovalStep step = mock(ApprovalStep.class);

        when(stepRepository.findById(14L))
                .thenReturn(Optional.of(step));

        when(step.getStatus())
                .thenReturn(ApprovalStepStatus.APPROVED);

        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> approvalService.approveStep(
                        14L,
                        mock(ApproveRequestDto.class)
                )
        );

        // Önceki adımlar sorgulanmadan mevcut adımın durumu kontrol edilir.
        verify(stepRepository, never())
                .findByRequestIdOrderByStepOrderAsc(100L);

        verify(stepRepository, never()).save(step);
    }

    @Test
    void aciklamasiBosOlanTalepReddedilememeli() {
        RejectRequestDto dto = mock(RejectRequestDto.class);

        when(dto.getDescription()).thenReturn("  ");

        assertThrows(
                BusinessRuleException.class,
                () -> approvalService.rejectStep(14L, dto)
        );

        // Açıklama geçersizse veritabanından adım aranmamalı.
        verify(stepRepository, never()).findById(14L);
    }

    @Test
    void bekleyenOnaylarGirisYapanCalisaninKimligiVeRoluneGoreSorgulanmali() {
        Employee approver = mock(Employee.class);
        Role role = mock(Role.class);

        when(currentUserService.getCurrentEmployeeId()).thenReturn(4L);
        when(employeeService.getById(4L)).thenReturn(approver);
        when(approver.getId()).thenReturn(4L);
        when(approver.getRole()).thenReturn(role);
        when(role.getId()).thenReturn(7L);

        when(stepRepository.findPendingStepsForApprover(
                4L, 7L, ApprovalStepStatus.PENDING
        )).thenReturn(List.of());

        var result = approvalService.getPendingStepsForApprover();

        assertEquals(0, result.size());

        verify(stepRepository).findPendingStepsForApprover(
                4L, 7L, ApprovalStepStatus.PENDING
        );
    }

    @Test
    void oncekiAdimiTamamlanmamisOnayBekleyenlerListesineEklenmemeli() {
        Employee approver = mock(Employee.class);
        Role role = mock(Role.class);
        Request request = mock(Request.class);

        ApprovalStep currentStep = mock(ApprovalStep.class);
        ApprovalStep previousStep = mock(ApprovalStep.class);

        when(currentUserService.getCurrentEmployeeId()).thenReturn(4L);
        when(employeeService.getById(4L)).thenReturn(approver);
        when(approver.getId()).thenReturn(4L);
        when(approver.getRole()).thenReturn(role);
        when(role.getId()).thenReturn(7L);

        when(stepRepository.findPendingStepsForApprover(
                4L, 7L, ApprovalStepStatus.PENDING
        )).thenReturn(List.of(currentStep));

        when(currentStep.getStepOrder()).thenReturn(2);
        when(currentStep.getRequest()).thenReturn(request);
        when(request.getId()).thenReturn(100L);

        when(previousStep.getStepOrder()).thenReturn(1);
        when(previousStep.getStatus()).thenReturn(ApprovalStepStatus.PENDING);

        when(stepRepository.findByRequestIdOrderByStepOrderAsc(100L))
                .thenReturn(List.of(previousStep, currentStep));

        var result = approvalService.getPendingStepsForApprover();

        assertEquals(0, result.size());
    }
}