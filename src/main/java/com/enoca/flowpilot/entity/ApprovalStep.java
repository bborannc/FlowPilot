package com.enoca.flowpilot.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "approval_steps")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ApprovalStep {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_role_id", nullable = false)
    private Role assignedRole;

    @Column(nullable = false)
    private Integer stepOrder; // 1, 2, 3...

    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REJECTED
}
