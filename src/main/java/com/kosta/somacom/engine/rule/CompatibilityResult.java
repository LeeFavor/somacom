package com.kosta.somacom.engine.rule;


import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.score.CompatibilityStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CompatibilityResult {

    private final CompatibilityStatus status;
    private final String reasonCode;
    private final BaseSpec partA;
    private final BaseSpec partB;
    
    public CompatibilityResult(CompatibilityStatus status, String reasonCode) {
		this.status = CompatibilityStatus.SUCCESS;
    	this.reasonCode = null;
    	this.partA = null;
    	this.partB = null;
    }

    public static CompatibilityResult success() {
        return new CompatibilityResult(CompatibilityStatus.SUCCESS, null);
    }
}