package com.enoca.flowpilot.repository;

import com.enoca.flowpilot.core.entities.ApprovalStep;
import com.enoca.flowpilot.core.enums.ApprovalStepStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {
    // Bir talebe ait adımları sıra numarasına göre getirir
    List<ApprovalStep> findByRequestIdOrderByStepOrderAsc(Long requestId);

    // Onaycının bekleyen taleplerini listeler (Doğrudan kullanıcıya atanmış veya rolüne atanmış adımlar)
    @Query("SELECT s FROM ApprovalStep s " +
            "JOIN FETCH s.request r " +
            "WHERE s.status = :status " +
            "AND (s.assignedEmployee.id = :employeeId OR (s.assignedEmployee IS NULL AND s.assignedRole.id = :roleId)) " +
            "ORDER BY r.createdAt DESC")
    List<ApprovalStep> findPendingStepsForApprover(
            @Param("employeeId") Long employeeId,
            @Param("roleId") Long roleId,
            @Param("status") ApprovalStepStatus status
    );

    // Bir talebin şu an bekleyen aktif adımını bulur
    Optional<ApprovalStep> findFirstByRequestIdAndStatusOrderByStepOrderAsc(Long requestId, ApprovalStepStatus status);
}
