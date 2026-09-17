package com.enoca.flowpilot.repository;

import com.enoca.flowpilot.core.entities.ApprovalStep;
import com.enoca.flowpilot.core.enums.ApprovalStepStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {

    List<ApprovalStep> findByRequestIdOrderByStepOrderAsc(Long requestId);

    @Query("SELECT s FROM ApprovalStep s " +
            "JOIN FETCH s.request r " +
            "JOIN FETCH r.employee " +
            "LEFT JOIN FETCH s.assignedRole role " +
            "LEFT JOIN FETCH s.assignedEmployee emp " +
            "WHERE s.status = :status " +
            "AND (" +
            "     (emp.id IS NOT NULL AND emp.id = :employeeId) " +
            "     OR " +
            "     (emp.id IS NULL AND role.id = :roleId)" +
            ")")
    List<ApprovalStep> findPendingStepsForApprover(
            @Param("employeeId") Long employeeId,
            @Param("roleId") Long roleId,
            @Param("status") ApprovalStepStatus status
    );
}