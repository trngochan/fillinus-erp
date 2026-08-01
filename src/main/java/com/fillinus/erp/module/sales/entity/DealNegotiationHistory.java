package com.fillinus.erp.module.sales.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Deal Negotiation History — one immutable entry of the negotiation timeline (V14).
 * BR-003: appended on every update, never edited afterwards.
 */
@Entity
@Table(name = "deal_negotiation_history")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DealNegotiationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_negotiation_id", nullable = false)
    private DealNegotiation dealNegotiation;

    @CreationTimestamp
    @Column(name = "discussion_date", updatable = false)
    private LocalDateTime discussionDate;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "discussion", nullable = false, columnDefinition = "TEXT")
    private String discussion;

    @Column(name = "customer_feedback", columnDefinition = "TEXT")
    private String customerFeedback;

    @Column(name = "next_action", columnDefinition = "TEXT")
    private String nextAction;

    @Column(name = "next_follow_up_date")
    private LocalDate nextFollowUpDate;
}
