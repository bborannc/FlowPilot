package com.enoca.flowpilot.repository;

import com.enoca.flowpilot.core.entities.ApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory, Long> {
    // Bir talebin geçmiş aksiyonlarını kronolojik sırayla listeler
    List<ApprovalHistory> findByRequestIdOrderByActionDateAsc(Long requestId);
}
