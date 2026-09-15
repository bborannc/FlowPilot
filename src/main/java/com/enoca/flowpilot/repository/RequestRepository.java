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
public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    List<Request> findByStatus(RequestStatus status);

    // MultipleBagFetchException'ı önlemek için tek collection (details) ve tekil entity (employee) fetch edilir
    @Query("SELECT DISTINCT r FROM Request r " +
            "JOIN FETCH r.employee " +
            "LEFT JOIN FETCH r.details " +
            "WHERE r.id = :id")
    Optional<Request> findByIdWithDetailsAndSteps(@Param("id") Long id);
}