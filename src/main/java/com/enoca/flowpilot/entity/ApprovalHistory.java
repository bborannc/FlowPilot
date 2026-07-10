package com.enoca.flowpilot.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "approval_histories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApprovalHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Aksiyonu alan kişi

    @Column(nullable = false)
    private String action; // SUBMIT, APPROVE, REJECT

    private String description; // Red açıklamaları için zorunlu alan

    private LocalDateTime actionDate;

    @PrePersist
    protected void onCreate() {
        this.actionDate = LocalDateTime.now();
    }
}
