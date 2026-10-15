package com.enoca.flowpilot.repository;

import com.enoca.flowpilot.core.entities.ApprovalStep;
import com.enoca.flowpilot.core.entities.Employee;
import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.core.entities.Role;
import com.enoca.flowpilot.core.enums.ApprovalStepStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.enoca.flowpilot.core.enums.RequestType;
import com.enoca.flowpilot.core.enums.RequestStatus;
import com.enoca.flowpilot.core.enums.RequestPriority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "JWT_SECRET=MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDE=",
        "spring.flyway.enabled=true",
        "spring.flyway.locations=classpath:db/migration"
})
@Testcontainers
@Transactional
class ApprovalStepRepositoryIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("flowpilot_repository_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add(
                "spring.datasource.driver-class-name",
                postgres::getDriverClassName
        );
    }

    @Autowired
    private ApprovalStepRepository stepRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void sorguCalisanRolVeDurumFiltreleriniDogruUygulamali() {

        // Test veritabanındaki örnek çalışanları al.
        List<Employee> employees = entityManager.createQuery(
                "SELECT e FROM Employee e ORDER BY e.id",
                Employee.class
        ).setMaxResults(2).getResultList();

        assertTrue(
                employees.size() >= 2,
                "Test için en az iki çalışan bulunmalı."
        );

        Employee currentEmployee = employees.get(0);
        Employee otherEmployee = employees.get(1);

        Role currentRole = currentEmployee.getRole();

        Long currentEmployeeId = currentEmployee.getId();
        Long currentRoleId = currentRole.getId();

        // Giriş yapan çalışanın rolünden farklı bir rol bul.
        List<Role> roles = entityManager.createQuery(
                "SELECT r FROM Role r ORDER BY r.id",
                Role.class
        ).getResultList();

        Role otherRole = roles.stream()
                .filter(role -> !role.getId().equals(currentRoleId))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError(
                                "Test için farklı bir rol bulunmalı."
                        )
                );

        Request request = Request.builder()
                .employee(currentEmployee)
                .requestType(RequestType.values()[0])
                .status(RequestStatus.values()[0])
                .priority(RequestPriority.values()[0])
                .build();

        entityManager.persist(request);
        entityManager.flush();

        // 1. Doğrudan giriş yapan çalışana atanmış adım.
        ApprovalStep assignedToCurrent = saveStep(
                request,
                currentEmployee,
                currentRole,
                ApprovalStepStatus.PENDING,
                101
        );

        // 2. Aynı role sahip olsa da başka çalışana atanmış adım.
        ApprovalStep assignedToOther = saveStep(
                request,
                otherEmployee,
                currentRole,
                ApprovalStepStatus.PENDING,
                102
        );

        // 3. Doğrudan çalışan ataması olmayan, aynı role atanmış adım.
        ApprovalStep assignedToRole = saveStep(
                request,
                null,
                currentRole,
                ApprovalStepStatus.PENDING,
                103
        );

        // 4. Başka bir role atanmış adım.
        ApprovalStep assignedToOtherRole = saveStep(
                request,
                null,
                otherRole,
                ApprovalStepStatus.PENDING,
                104
        );

        // 5. Aynı role atanmış olsa da artık beklemede olmayan adım.
        ApprovalStep approvedStep = saveStep(
                request,
                null,
                currentRole,
                ApprovalStepStatus.APPROVED,
                105
        );

        // Kimlikleri sorgudan önce sakla.
        Long currentStepId = assignedToCurrent.getId();
        Long otherEmployeeStepId = assignedToOther.getId();
        Long roleStepId = assignedToRole.getId();
        Long otherRoleStepId = assignedToOtherRole.getId();
        Long approvedStepId = approvedStep.getId();

        // Sorgunun mevcut persistence context yerine veritabanından
        // okuyacağını doğrulamak için kayıtları flush edip context'i temizle.
        entityManager.flush();
        entityManager.clear();

        List<ApprovalStep> results =
                stepRepository.findPendingStepsForApprover(
                        currentEmployeeId,
                        currentRoleId,
                        ApprovalStepStatus.PENDING
                );

        Set<Long> resultIds = results.stream()
                .map(ApprovalStep::getId)
                .collect(Collectors.toSet());

        // Doğrudan çalışana atanmış adım bulunmalı.
        assertTrue(resultIds.contains(currentStepId));

        // Çalışan doğrudan başka birine atanmış adımı görmemeli.
        assertFalse(resultIds.contains(otherEmployeeStepId));

        // Aynı role atanmış ve doğrudan çalışanı olmayan adım bulunmalı.
        assertTrue(resultIds.contains(roleStepId));

        // Başka role atanmış adım bulunmamalı.
        assertFalse(resultIds.contains(otherRoleStepId));

        // PENDING olmayan adım bulunmamalı.
        assertFalse(resultIds.contains(approvedStepId));

    }

    private ApprovalStep saveStep(
            Request request,
            Employee assignedEmployee,
            Role assignedRole,
            ApprovalStepStatus status,
            int stepOrder
    ) {
        ApprovalStep step = ApprovalStep.builder()
                .request(request)
                .assignedEmployee(assignedEmployee)
                .assignedRole(assignedRole)
                .status(status)
                .stepOrder(stepOrder)
                .build();

        return stepRepository.saveAndFlush(step);
    }

}