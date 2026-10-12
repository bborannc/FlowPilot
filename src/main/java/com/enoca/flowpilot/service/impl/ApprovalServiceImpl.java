package com.enoca.flowpilot.service.impl;

import com.enoca.flowpilot.core.entities.*;
import com.enoca.flowpilot.core.enums.ApprovalAction;
import com.enoca.flowpilot.core.enums.ApprovalStepStatus;
import com.enoca.flowpilot.core.enums.RequestStatus;
import com.enoca.flowpilot.core.exceptions.BusinessRuleException;
import com.enoca.flowpilot.core.exceptions.ResourceNotFoundException;
import com.enoca.flowpilot.core.exceptions.UnauthorizedApprovalException;
import com.enoca.flowpilot.dto.request.ApproveRequestDto;
import com.enoca.flowpilot.dto.request.RejectRequestDto;
import com.enoca.flowpilot.dto.response.ApprovalHistoryResponseDto;
import com.enoca.flowpilot.dto.response.ApprovalStepResponseDto;
import com.enoca.flowpilot.repository.ApprovalHistoryRepository;
import com.enoca.flowpilot.repository.ApprovalStepRepository;
import com.enoca.flowpilot.repository.RequestRepository;
import com.enoca.flowpilot.repository.RoleRepository;
import com.enoca.flowpilot.service.ApprovalService;
import com.enoca.flowpilot.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.enoca.flowpilot.security.ApprovalAuthorizationService;
import com.enoca.flowpilot.security.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalStepRepository stepRepository;
    private final ApprovalHistoryRepository historyRepository;
    private final RequestRepository requestRepository;
    private final RoleRepository roleRepository;
    private final EmployeeService employeeService;
    private final ApprovalAuthorizationService approvalAuthorizationService;
    private final CurrentUserService currentUserService;

    @Override
    @Transactional
    public void createApprovalStep(Request request, String roleName, Long assignedEmployeeId, int stepOrder) {
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Rol bulunamadı: " + roleName));

        Employee assignedEmployee = (assignedEmployeeId != null)
                ? employeeService.getById(assignedEmployeeId)
                : null;

        ApprovalStep step = ApprovalStep.builder()
                .request(request)
                .assignedRole(role)
                .assignedEmployee(assignedEmployee)
                .stepOrder(stepOrder)
                .status(ApprovalStepStatus.PENDING)
                .build();

        stepRepository.save(step);
    }

    @Override
    @Transactional
    public void recordHistory(Request request, Long employeeId, ApprovalAction action, String description) {
        Employee employee = employeeService.getById(employeeId);

        ApprovalHistory history = ApprovalHistory.builder()
                .request(request)
                .employee(employee)
                .action(action)
                .description(description)
                .build();

        historyRepository.save(history);
    }

    @Override
    @Transactional
    public void approveStep(Long stepId, ApproveRequestDto dto) {

        ApprovalStep step = stepRepository.findById(stepId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Onay adımı bulunamadı: " + stepId
                        )
                );

        // 1. Onaylayan kullanıcının gerçekten yetkili olduğunu doğrula.
        approvalAuthorizationService.assertCanActOnStep(step);

        // 2. Yalnızca PENDING durumundaki adımlar onaylanabilir.
        if (step.getStatus() != ApprovalStepStatus.PENDING) {
            throw new BusinessRuleException(
                    "Yalnızca beklemede (PENDING) olan adımlar onaylanabilir."
            );
        }

        // 3. Önceki adımların tamamlanmış olduğunu doğrula.
        if (!isPreviousStepsCompleted(step)) {
            throw new BusinessRuleException(
                    "Önceki onay adımları tamamlanmadan bu adım onaylanamaz."
            );
        }

        // 4. Onaylayan çalışanı request'ten değil, JWT'den bul.
        Long currentEmployeeId = currentUserService.getCurrentEmployeeId();
        Employee approver = employeeService.getById(currentEmployeeId);

        // 5. Onay adımını güncelle.
        step.setStatus(ApprovalStepStatus.APPROVED);
        stepRepository.save(step);

        Request request = step.getRequest();

        String comment =
                dto.getDescription() != null
                        && !dto.getDescription().isBlank()
                        ? dto.getDescription()
                        : "Adım " + step.getStepOrder() + " onaylandı.";

        recordHistory(
                request,
                approver.getId(),
                ApprovalAction.APPROVED,
                comment
        );

        // 6. Diğer onay adımlarının durumlarını kontrol et.
        List<ApprovalStep> allSteps =
                stepRepository.findByRequestIdOrderByStepOrderAsc(request.getId());

        boolean hasPendingSteps = allSteps.stream()
                .anyMatch(s -> s.getStatus() == ApprovalStepStatus.PENDING);

        // 7. Bekleyen adım kalmadıysa talebi onayla.
        if (!hasPendingSteps) {
            request.setStatus(RequestStatus.APPROVED);
            requestRepository.save(request);

            recordHistory(
                    request,
                    approver.getId(),
                    ApprovalAction.APPROVED,
                    "Tüm onay süreçleri tamamlandı, talep onaylandı."
            );
        }
    }

    @Override
    @Transactional
    public void rejectStep(Long stepId, RejectRequestDto dto) {

        if (dto.getDescription() == null
                || dto.getDescription().isBlank()) {
            throw new BusinessRuleException(
                    "Reddetme işleminde açıklama girilmesi zorunludur."
            );
        }

        ApprovalStep step = stepRepository.findById(stepId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Onay adımı bulunamadı: " + stepId
                        )
                );

        // Kullanıcının bu onay adımına atanmış olup olmadığını kontrol et.
        approvalAuthorizationService.assertCanActOnStep(step);

        if (step.getStatus() != ApprovalStepStatus.PENDING) {
            throw new BusinessRuleException(
                    "Yalnızca beklemede (PENDING) olan adımlar reddedilebilir."
            );
        }

        if (!isPreviousStepsCompleted(step)) {
            throw new BusinessRuleException(
                    "Önceki onay adımları tamamlanmadan bu işlem yapılamaz."
            );
        }

        // Reddeden çalışan JWT'den belirlenir.
        Long currentEmployeeId = currentUserService.getCurrentEmployeeId();
        Employee approver = employeeService.getById(currentEmployeeId);

        // Mevcut adımı reddet.
        step.setStatus(ApprovalStepStatus.REJECTED);
        stepRepository.save(step);

        // Talebi reddet.
        Request request = step.getRequest();
        request.setStatus(RequestStatus.REJECTED);
        requestRepository.save(request);

        // Bekleyen diğer adımları iptal et.
        List<ApprovalStep> allSteps =
                stepRepository.findByRequestIdOrderByStepOrderAsc(request.getId());

        for (ApprovalStep otherStep : allSteps) {
            if (otherStep.getStatus() == ApprovalStepStatus.PENDING) {
                otherStep.setStatus(ApprovalStepStatus.CANCELLED);
                stepRepository.save(otherStep);
            }
        }

        recordHistory(
                request,
                approver.getId(),
                ApprovalAction.REJECTED,
                dto.getDescription()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalStepResponseDto> getPendingStepsForApprover() {

        Long currentEmployeeId = currentUserService.getCurrentEmployeeId();

        Employee approver = employeeService.getById(currentEmployeeId);

        Long roleId = approver.getRole() != null
                ? approver.getRole().getId()
                : -1L;

        List<ApprovalStep> pendingSteps =
                stepRepository.findPendingStepsForApprover(
                        approver.getId(),
                        roleId,
                        ApprovalStepStatus.PENDING
                );

        return pendingSteps.stream()
                .filter(this::isPreviousStepsCompleted)
                .map(step -> ApprovalStepResponseDto.builder()
                        .stepId(step.getId())
                        .requestId(step.getRequest().getId())
                        .requestType(step.getRequest().getRequestType().name())
                        .requesterName(step.getRequest().getEmployee().getName())
                        .stepOrder(step.getStepOrder())
                        .status(step.getStatus())
                        .roleName(
                                step.getAssignedRole() != null
                                        ? step.getAssignedRole().getName()
                                        : null
                        )
                        .requestCreatedAt(step.getRequest().getCreatedAt())
                        .build()
                )
                .collect(Collectors.toList());
    }

    private boolean isPreviousStepsCompleted(ApprovalStep currentStep) {
        if (currentStep.getStepOrder() == 1) {
            return true;
        }
        List<ApprovalStep> allSteps = stepRepository.findByRequestIdOrderByStepOrderAsc(currentStep.getRequest().getId());
        return allSteps.stream()
                .filter(s -> s.getStepOrder() < currentStep.getStepOrder())
                .allMatch(s -> s.getStatus() == ApprovalStepStatus.APPROVED);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalHistoryResponseDto> getRequestHistory(Long requestId) {

        // 1. Talebin mevcut olup olmadığını kontrol et.
        Request request = requestRepository.findById(requestId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Talep bulunamadı: " + requestId
                        )
                );

        // 2. Giriş yapan çalışanın ID'sini JWT'den al.
        Long currentEmployeeId = currentUserService.getCurrentEmployeeId();

        // 3. Çalışan yalnızca kendi talebinin geçmişini görüntüleyebilir.
        if (request.getEmployee() == null
                || !Objects.equals(
                request.getEmployee().getId(),
                currentEmployeeId
        )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Bu talebin onay geçmişini görüntüleme yetkiniz yok."
            );
        }

        // 4. Yetki kontrolünden sonra geçmiş kayıtlarını getir.
        return historyRepository
                .findByRequestIdOrderByActionDateAsc(requestId)
                .stream()
                .map(h -> ApprovalHistoryResponseDto.builder()
                        .id(h.getId())
                        .employeeName(h.getEmployee().getName())
                        .action(h.getAction())
                        .description(h.getDescription())
                        .actionDate(h.getActionDate())
                        .build()
                )
                .collect(Collectors.toList());
    }
}