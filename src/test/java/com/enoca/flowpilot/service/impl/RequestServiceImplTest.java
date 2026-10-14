package com.enoca.flowpilot.service.impl;

import com.enoca.flowpilot.core.entities.Employee;
import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.repository.RequestRepository;
import com.enoca.flowpilot.security.CurrentUserService;
import com.enoca.flowpilot.service.ApprovalService;
import com.enoca.flowpilot.service.EmployeeService;
import com.enoca.flowpilot.service.policy.ApprovalPolicyRegistry;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.web.server.ResponseStatusException;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private ApprovalService approvalService;

    @Mock
    private ApprovalPolicyRegistry policyRegistry;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private RequestServiceImpl requestService;

    @Test
    void baskaCalisaninTalebiGoruntulenememeli() {

        // Giriş yapan çalışan: 4
        when(currentUserService.getCurrentEmployeeId())
                .thenReturn(4L);

        // Talep ID: 99
        Request request = org.mockito.Mockito.mock(Request.class);
        Employee owner = org.mockito.Mockito.mock(Employee.class);

        when(requestRepository.findByIdWithDetailsAndSteps(99L))
                .thenReturn(Optional.of(request));

        when(request.getEmployee()).thenReturn(owner);
        when(owner.getId()).thenReturn(9L);

        // 4 numaralı çalışan, 9 numaralı çalışanın
        // talebini görüntülemeye çalışıyor.
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> requestService.getRequestDetails(99L)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void baskaCalisaninTalebiOnayaSunulamamali() {

        when(currentUserService.getCurrentEmployeeId())
                .thenReturn(4L);

        Request request = org.mockito.Mockito.mock(Request.class);
        Employee owner = org.mockito.Mockito.mock(Employee.class);

        when(requestRepository.findByIdWithDetailsAndSteps(99L))
                .thenReturn(Optional.of(request));

        when(request.getEmployee()).thenReturn(owner);
        when(owner.getId()).thenReturn(9L);

        // Sahiplik kontrolü başarısız olmalı ve işlem
        // onay politikasına ulaşmadan durmalı.
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> requestService.submitRequest(99L)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void taleplerListelenirkenJwtKimligiKullanilmali() {

        when(currentUserService.getCurrentEmployeeId())
                .thenReturn(4L);

        when(requestRepository.findByEmployeeIdOrderByCreatedAtDesc(4L))
                .thenReturn(List.of());

        List<?> result = requestService.getMyRequests();

        assertEquals(0, result.size());

        verify(requestRepository)
                .findByEmployeeIdOrderByCreatedAtDesc(4L);
    }
}