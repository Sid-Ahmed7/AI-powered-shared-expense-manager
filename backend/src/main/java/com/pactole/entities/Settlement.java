package com.pactole.entities;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "settlement")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter 
public class Settlement {    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "from_member_id", nullable = false)
    private UUID fromMemberId;

    @Column(name = "to_member_id", nullable = false)
    private UUID toMemberId;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(name = "settled_on", nullable = false)
    private LocalDate settledOn;

    private String note;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();
}
