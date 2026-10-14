package com.pactole.entities;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.pactole.enums.Category;
import com.pactole.enums.Source;
import com.pactole.enums.SplitMode;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "expenses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Expense {

    @Embeddable
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    public static class Share {
        @Column(name = "member_id", nullable = false)
        private UUID memberId;

        @Column(name = "amount_cents", nullable = false)
        private long amountCents;

        private BigDecimal weight;
    }

    public record Item(String label, long amountCents, List<UUID> memberIds) {}

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(nullable = false)
    private String description;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(name = "payer_member_id", nullable = false)
    private UUID payerMemberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "split_mode", nullable = false)
    private SplitMode splitMode;

    @Column(name = "expense_date", nullable = false)
    private LocalDate date;

    @JdbcTypeCode(SqlTypes.JSON)
    private List<Item> items;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Source source = Source.MANUAL;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "expense_shares", joinColumns = @JoinColumn(name = "expense_id"))
    private List<Share> shares = new ArrayList<>();

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Version
    private long version;

    public Expense(UUID groupId, UUID createdBy) {
        this.groupId = groupId;
        this.createdBy = createdBy;
    }

    public void apply(String description, long amountCents, UUID payerMemberId, Category category, SplitMode splitMode, LocalDate date, List<Item> items, Source source, List<Share> newShares, UUID editorId) {
        this.description = description;
        this.amountCents = amountCents;
        this.payerMemberId = payerMemberId;
        this.category = category;
        this.splitMode = splitMode;
        this.date = date;
        this.items = items;
        this.source = source;
        this.shares.clear();
        this.shares.addAll(newShares);
        this.updatedBy = editorId;
        this.updatedAt = Instant.now();
    }
}
