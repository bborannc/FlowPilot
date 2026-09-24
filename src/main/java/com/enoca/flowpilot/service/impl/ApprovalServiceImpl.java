package com.enoca.flowpilot.service.impl;

import com.enoca.flowpilot.core.entities.*;
import com.enoca.flowpilot.core.enums.ApprovalAction;
import com.enoca.flowpilot.core.enums.ApprovalStepStatus;
import com.enoca.flowpilot.core.enums.RequestStatus;
import com.enoca.flowpilot.dto.request.ApproveRequestDto;
import com.enoca.flowpilot.dto.request.RejectRequestDto;
import com.enoca.flowpilot.dto.response.ApprovalHistoryResponseDto;
import com.enoca.flowpilot.dto.response.ApprovalStepResponseDto;
import com.enoca.flowpilot.exception.ResourceNotFoundException;
import com.enoca.flowpilot.repository.ApprovalHistoryRepository;
import com.enoca.flowpilot.repository.ApprovalStepRepository;
import com.enoca.flowpilot.repository.RequestRepository;
import com.enoca.flowpilot.repository.RoleRepository;
import com.enoca.flowpilot.service.ApprovalService;
import com.enoca.flowpilot.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new ResourceNotFoundException("Onay adımı bulunamadı: " + stepId));

        if (step.getStatus() != ApprovalStepStatus.PENDING) {
            throw new IllegalStateException("Yalnızca beklemede (PENDING) olan adımlar onaylanabilir.");
        }

        // Kural 1: Backend seviyesinde sıralı adım kontrolü (Önceki adımlar bitmeden bu adım onaylanamaz)
        if (!isPreviousStepsCompleted(step)) {
            throw new IllegalStateException("Önceki onay adımları tamamlanmadan bu adım onaylanamaz.");
        }

        // Kural 2: Doğrudan atama önceliği olan yetki doğrulaması
        Employee approver = employeeService.getById(dto.getApproverId());
        validateApproverAuthority(step, approver);

        step.setStatus(ApprovalStepStatus.APPROVED);
        stepRepository.save(step);

        Request request = step.getRequest();
        String comment = (dto.getDescription() != null && !dto.getDescription().isBlank())
                ? dto.getDescription()
                : "Adım " + step.getStepOrder() + " onaylandı.";
        recordHistory(request, approver.getId(), ApprovalAction.APPROVED, comment);

        List<ApprovalStep> allSteps = stepRepository.findByRequestIdOrderByStepOrderAsc(request.getId());
        boolean hasPendingSteps = allSteps.stream()
                .anyMatch(s -> s.getStatus() == ApprovalStepStatus.PENDING);

        if (!hasPendingSteps) {
            request.setStatus(RequestStatus.APPROVED);
            requestRepository.save(request);
            recordHistory(request, approver.getId(), ApprovalAction.APPROVED, "Tüm onay süreçleri tamamlandı, talep onaylandı.");
        }
    }

    @Override
    @Transactional
    public void rejectStep(Long stepId, RejectRequestDto dto) {
        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("Reddetme işleminde açıklama girilmesi zorunludur.");
        }

        ApprovalStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new ResourceNotFoundException("Onay adımı bulunamadı: " + stepId));

        if (step.getStatus() != ApprovalStepStatus.PENDING) {
            throw new IllegalStateException("Yalnızca beklemede (PENDING) olan adımlar reddedilebilir.");
        }

        // Kural 1: Backend seviyesinde sıralı adım kontrolü (Sırası gelmeyen adım reddedilemez)
        if (!isPreviousStepsCompleted(step)) {
            throw new IllegalStateException("Önceki onay adımları tamamlanmadan bu işlem yapılamaz.");
        }

        // Kural 2: Yetki doğrulaması
        Employee approver = employeeService.getById(dto.getApproverId());
        validateApproverAuthority(step, approver);

        // Mevcut adımı reddet
        step.setStatus(ApprovalStepStatus.REJECTED);
        stepRepository.save(step);

        // Talebi reddet
        Request request = step.getRequest();
        request.setStatus(RequestStatus.REJECTED);
        requestRepository.save(request);

        // Kural 3: Kalan tüm PENDING adımları CANCELLED statüsüne çek
        List<ApprovalStep> allSteps = stepRepository.findByRequestIdOrderByStepOrderAsc(request.getId());
        for (ApprovalStep otherStep : allSteps) {
            if (otherStep.getStatus() == ApprovalStepStatus.PENDING) {
                otherStep.setStatus(ApprovalStepStatus.CANCELLED);
                stepRepository.save(otherStep);
            }
        }

        recordHistory(request, approver.getId(), ApprovalAction.REJECTED, dto.getDescription());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalStepResponseDto> getPendingStepsForApprover(Long employeeId) {
        Employee approver = employeeService.getById(employeeId);
        Long roleId = (approver.getRole() != null) ? approver.getRole().getId() : -1L;

        List<ApprovalStep> pendingSteps = stepRepository.findPendingStepsForApprover(
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
                        .roleName(step.getAssignedRole() != null ? step.getAssignedRole().getName() : null)
                        .requestCreatedAt(step.getRequest().getCreatedAt())
                        .build()
                ).collect(Collectors.toList());
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
        return historyRepository.findByRequestIdOrderByActionDateAsc(requestId).stream()
                .map(h -> ApprovalHistoryResponseDto.builder()
                        .id(h.getId())
                        .employeeName(h.getEmployee().getName())
                        .action(h.getAction())
                        .description(h.getDescription())
                        .actionDate(h.getActionDate())
                        .build()
                ).collect(Collectors.toList());
    }

    /**
     * Doğrudan Atama Kuralı:
     * Adımda assignedEmployee belirtilmişse SADECE o kullanıcı işlem yapabilir;
     * aynı rolde olsa bile başka bir kullanıcı işlemi gerçekleştiremez.
     */
    private void validateApproverAuthority(ApprovalStep step, Employee approver) {
        if (step.getAssignedEmployee() != null) {
            if (!step.getAssignedEmployee().getId().equals(approver.getId())) {
                throw new SecurityException("Bu onay adımı doğrudan başka bir kullanıcıya atanmıştır. Onaylama yetkiniz bulunmamaktadır.");
            }
        } else if (step.getAssignedRole() != null) {
            if (approver.getRole() == null || !step.getAssignedRole().getId().equals(approver.getRole().getId())) {
                throw new SecurityException("Bu onay adımı için gerekli role sahip değilsiniz.");
            }
        } else {
            throw new SecurityException("Bu adım için yetkili tanımlanmamıştır.");
        }
    }
}