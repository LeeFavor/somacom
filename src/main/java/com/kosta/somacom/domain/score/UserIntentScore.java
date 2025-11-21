package com.kosta.somacom.domain.score;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_intent_score")
@IdClass(UserIntentScoreId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserIntentScore {

    @Id
    @Column(name = "user_id", length = 100)
    private String userId;

    @Id
    @Column(length = 100)
    private String category;

    @Id
    @Column(name = "attribute_tag", length = 100)
    private String attributeTag;

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

    @Builder
    public UserIntentScore(String userId, String category, String attributeTag, int viewCount, int longViewCount, int imageViewCount, int searchCount, int filterCount, int wishlistCount, int cartCount, int purchaseCount) {
        this.userId = userId;
        this.category = category;
        this.attributeTag = attributeTag;
        this.viewCount = viewCount;
        this.longViewCount = longViewCount;
        this.imageViewCount = imageViewCount;
        this.searchCount = searchCount;
        this.filterCount = filterCount;
        this.wishlistCount = wishlistCount;
        this.cartCount = cartCount;
        this.purchaseCount = purchaseCount;
    }
}