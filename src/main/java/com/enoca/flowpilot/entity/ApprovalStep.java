package com.enoca.flowpilot.entity;

import com.enoca.flowpilot.enums.StepStatus;
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

    // "Bu adım hangi spesifik kullanıcıya atandı?" sorusunun cevabı
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_employee_id")
    private Employee assignedEmployee;

    @Column(nullable = false)
    private Integer stepOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StepStatus status;
}
