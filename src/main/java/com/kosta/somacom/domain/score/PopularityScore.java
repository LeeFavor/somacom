package com.kosta.somacom.domain.score;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_popularity_scores")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PopularityScore {

    @EmbeddedId
    private PopularityScoreId id;

    private Long score;

    @UpdateTimestamp
    private LocalDateTime lastCalculatedAt;

    public void addScore(long count) {
        this.score += count;
    }
}