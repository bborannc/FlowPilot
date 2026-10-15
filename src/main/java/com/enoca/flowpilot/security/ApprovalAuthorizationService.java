package com.enoca.flowpilot.security;

import com.enoca.flowpilot.core.entities.ApprovalStep;
import com.enoca.flowpilot.core.entities.Employee;
import com.enoca.flowpilot.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ApprovalAuthorizationService {

    private final CurrentUserService currentUserService;
    private final EmployeeService employeeService;

    public void assertCanActOnStep(ApprovalStep step) {

        Long currentEmployeeId =
                currentUserService.getCurrentEmployeeId();

        // Çalışana özel atanmış adım:
        // Yalnızca atanmış çalışan işlem yapabilir.
        if (step.getAssignedEmployee() != null) {

            Long assignedEmployeeId =
                    step.getAssignedEmployee().getId();

            if (!Objects.equals(
                    assignedEmployeeId,
                    currentEmployeeId
            )) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Bu onay adımında işlem yapma yetkiniz yok."
                );
            }

            return;
        }

        if (step.getAssignedRole() != null) {

            Employee currentEmployee =
                    employeeService.getById(currentEmployeeId);

            Long currentRoleId = null;

            if (currentEmployee != null
                    && currentEmployee.getRole() != null) {
                currentRoleId = currentEmployee.getRole().getId();
            }

            Long assignedRoleId =
                    step.getAssignedRole().getId();

            if (currentRoleId != null
                    && assignedRoleId != null
                    && Objects.equals(currentRoleId, assignedRoleId)) {
                return;
            }
        }

        // Atanmış çalışan veya rol ile eşleşme yoksa erişimi engelle.
        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Bu onay adımında işlem yapma yetkiniz yok."
        );
    }
}