package com.kosta.somacom.dto.request;

import com.kosta.somacom.domain.request.BaseSpecRequestStatus;
import lombok.Data;

@Data
public class BaseSpecRequestProcessDto {
    private BaseSpecRequestStatus status; // APPROVED or REJECTED
    private String adminNotes; // 거절 사유 등
}