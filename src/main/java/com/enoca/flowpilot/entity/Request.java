package com.enoca.flowpilot.entity;

import com.enoca.flowpilot.enums.RequestPriority;
import com.enoca.flowpilot.enums.RequestStatus;
import com.enoca.flowpilot.enums.RequestType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestPriority priority;

    private LocalDateTime createdAt;

    // Tip güvenliği (Generic List) sağlandı
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RequestDetail> details = new ArrayList<>();

    // Okunabilirliği artırmak için eklenen çift yönlü ilişkiler
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ApprovalStep> approvalSteps = new ArrayList<>();

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ApprovalHistory> approvalHistories = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}