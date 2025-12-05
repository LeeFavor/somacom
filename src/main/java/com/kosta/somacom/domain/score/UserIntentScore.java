package com.kosta.somacom.domain.score;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_intent_score")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserIntentScore {

    @EmbeddedId
    private UserIntentScoreId id;

    private int viewCount;
    private int longViewCount;
    private int imageViewCount;
    private int searchCount;
    private int filterCount;
    private int wishlistCount;
    private int cartCount;
    private int purchaseCount;

    @UpdateTimestamp
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

}