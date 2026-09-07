package com.enoca.flowpilot.repository;

import com.enoca.flowpilot.core.entities.Request;
import com.enoca.flowpilot.core.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request,Long> {
    // Kullanıcının kendi taleplerini tarihe göre yeniden eskiye listeler
    List<Request> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    // Statüye göre filtreleme
    List<Request> findByStatus(RequestStatus status);

    // N+1 problemini önlemek için detayları, adımları ve tarihçesiyle birlikte tek sorguda çeken metot
    @Query("SELECT r FROM Request r " +
            "LEFT JOIN FETCH r.details " +
            "LEFT JOIN FETCH r.approvalSteps " +
            "WHERE r.id = :id")
    Optional<Request> findByIdWithDetailsAndSteps(@Param("id") Long id);
}
