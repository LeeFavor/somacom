package com.kosta.somacom.domain.score;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_compatibility_scores")
@IdClass(CompatibilityScoreId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompatibilityScore {

    @Id
    @Column(name = "spec_a_id", length = 100)
    private String specAId;

    @Id
    @Column(name = "spec_b_id", length = 100)
    private String specBId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompatibilityStatus status;

    @Column(name = "reason_code")
    private String reasonCode;

    @UpdateTimestamp
    @Column(name = "last_checked_at")
    private LocalDateTime lastCheckedAt;

    @Builder
    public CompatibilityScore(String specAId, String specBId, CompatibilityStatus status, String reasonCode) {
        this.specAId = specAId;
        this.specBId = specBId;
        this.status = status;
        this.reasonCode = reasonCode;
    }
}