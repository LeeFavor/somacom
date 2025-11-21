package com.kosta.somacom.domain.score;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_popularity_scores")
@IdClass(PopularityScoreId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PopularityScore {

    @Id
    @Column(name = "spec_a_id", length = 100)
    private String specAId;

    @Id
    @Column(name = "spec_b_id", length = 100)
    private String specBId;

    @Column(nullable = false)
    private Long score;

    @UpdateTimestamp
    @Column(name = "last_calculated_at")
    private LocalDateTime lastCalculatedAt;

    @Builder
    public PopularityScore(String specAId, String specBId, Long score) {
        this.specAId = specAId;
        this.specBId = specBId;
        this.score = score;
    }
}