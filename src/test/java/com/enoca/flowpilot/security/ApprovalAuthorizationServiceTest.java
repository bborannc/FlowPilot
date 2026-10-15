package com.enoca.flowpilot.security;

import com.enoca.flowpilot.core.entities.ApprovalStep;
import com.enoca.flowpilot.core.entities.Employee;
import com.enoca.flowpilot.core.entities.Role;
import com.enoca.flowpilot.service.EmployeeService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApprovalAuthorizationServiceTest {

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private EmployeeService employeeService;

    @InjectMocks
    private ApprovalAuthorizationService authorizationService;

    @Mock
    private ApprovalStep step;

    @Mock
    private Employee assignedEmployee;

    @Mock
    private Employee currentEmployee;

    @Mock
    private Role assignedRole;

    @Mock
    private Role currentRole;

    @Test
    void kendisineAtananCalisanOnayAdimindaIslemYapabilmeli() {
        when(currentUserService.getCurrentEmployeeId()).thenReturn(4L);
        when(step.getAssignedEmployee()).thenReturn(assignedEmployee);
        when(assignedEmployee.getId()).thenReturn(4L);

        assertDoesNotThrow(
                () -> authorizationService.assertCanActOnStep(step)
        );
    }

    @Test
    void baskaCalisanaAtananAdimdaIslemYapilamamali() {
        when(currentUserService.getCurrentEmployeeId()).thenReturn(4L);
        when(step.getAssignedEmployee()).thenReturn(assignedEmployee);
        when(assignedEmployee.getId()).thenReturn(9L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authorizationService.assertCanActOnStep(step)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void ayniRoleSahipCalisanRolAtamaliAdimdaIslemYapabilmeli() {
        when(currentUserService.getCurrentEmployeeId()).thenReturn(4L);
        when(step.getAssignedEmployee()).thenReturn(null);
        when(step.getAssignedRole()).thenReturn(assignedRole);

        when(employeeService.getById(4L)).thenReturn(currentEmployee);
        when(currentEmployee.getRole()).thenReturn(currentRole);
        when(currentRole.getId()).thenReturn(7L);
        when(assignedRole.getId()).thenReturn(7L);

        assertDoesNotThrow(
                () -> authorizationService.assertCanActOnStep(step)
        );
    }

    @Test
    void farkliRoleSahipCalisanIslemYapamamali() {
        when(currentUserService.getCurrentEmployeeId()).thenReturn(4L);
        when(step.getAssignedEmployee()).thenReturn(null);
        when(step.getAssignedRole()).thenReturn(assignedRole);

        when(employeeService.getById(4L)).thenReturn(currentEmployee);
        when(currentEmployee.getRole()).thenReturn(currentRole);
        when(currentRole.getId()).thenReturn(7L);
        when(assignedRole.getId()).thenReturn(8L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authorizationService.assertCanActOnStep(step)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void calisanaVeyaRoleAtanmamisAdimdaIslemYapilamamali() {
        when(currentUserService.getCurrentEmployeeId()).thenReturn(4L);
        when(step.getAssignedEmployee()).thenReturn(null);
        when(step.getAssignedRole()).thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authorizationService.assertCanActOnStep(step)
        );

        assertEquals(403, exception.getStatusCode().value());
    }

    @Test
    void roluOlmayanCalisanOnayAdimindaIslemYapamamali() {
        when(currentUserService.getCurrentEmployeeId()).thenReturn(4L);

        when(step.getAssignedEmployee()).thenReturn(null);
        when(step.getAssignedRole()).thenReturn(assignedRole);

        when(employeeService.getById(4L)).thenReturn(currentEmployee);
        when(currentEmployee.getRole()).thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authorizationService.assertCanActOnStep(step)
        );

        assertEquals(403, exception.getStatusCode().value());
    }
}