package com.kosta.somacom.domain.score;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserIntentScoreId implements Serializable {

    private String userId;
    private String category;
    private String attributeTag;

}