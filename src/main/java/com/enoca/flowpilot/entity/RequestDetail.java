package com.enoca.flowpilot.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "request_details")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RequestDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private Request request;

    @Column(name = "detail_key", nullable = false)
    private String key; // örn: "amount", "totalDays", "description"

    @Column(name = "detail_value", nullable = false)
    private String value; // örn: "6000", "5", "MacBook Pro Talebi"
}
