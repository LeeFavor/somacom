package com.kosta.somacom.domain.score;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PopularityScoreId implements Serializable {

    private String specAId;
    private String specBId;

}