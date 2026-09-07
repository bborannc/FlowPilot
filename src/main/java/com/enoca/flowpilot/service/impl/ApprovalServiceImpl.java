package com.enoca.flowpilot.service.impl;

import com.enoca.flowpilot.core.entities.*;
import com.enoca.flowpilot.core.enums.ApprovalAction;
import com.enoca.flowpilot.core.enums.ApprovalStepStatus;
import com.enoca.flowpilot.exception.ResourceNotFoundException;
import com.enoca.flowpilot.repository.ApprovalHistoryRepository;
import com.enoca.flowpilot.repository.ApprovalStepRepository;
import com.enoca.flowpilot.repository.RoleRepository;
import com.enoca.flowpilot.service.ApprovalService;
import com.enoca.flowpilot.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalStepRepository stepRepository;
    private final ApprovalHistoryRepository historyRepository;
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
    @Transactional(readOnly = true)
    public List<ApprovalStep> getPendingStepsForApprover(Long employeeId) {
        Employee approver = employeeService.getById(employeeId);
        return stepRepository.findPendingStepsForApprover(
                approver.getId(),
                approver.getRole().getId(),
                ApprovalStepStatus.PENDING
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalHistory> getRequestHistory(Long requestId) {
        return historyRepository.findByRequestIdOrderByActionDateAsc(requestId);
    }
}