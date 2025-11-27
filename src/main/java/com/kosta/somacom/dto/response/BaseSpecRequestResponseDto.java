package com.kosta.somacom.dto.response;

import com.kosta.somacom.domain.part.PartCategory;
import com.kosta.somacom.domain.user.User;
import com.kosta.somacom.domain.request.BaseSpecRequest;
import com.kosta.somacom.domain.request.BaseSpecRequestStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BaseSpecRequestResponseDto {
    private Long requestId;
    private String sellerName; // 상호명
    private String requestedModelName;
    private PartCategory category;
    private String manufacturer;
    private BaseSpecRequestStatus status;
    private LocalDateTime requestedAt;

    public BaseSpecRequestResponseDto(BaseSpecRequest request) {
        this.requestId = request.getId();
        // sellerInfo가 없는 경우를 대비한 방어 코드
        if (request.getSeller() != null && request.getSeller().getSellerInfo() != null) {
            this.sellerName = request.getSeller().getSellerInfo().getCompanyName();
        } else {
            this.sellerName = "알 수 없는 판매자";
        }
        this.requestedModelName = request.getRequestedModelName();
        this.category = request.getCategory();
        this.manufacturer = request.getManufacturer();
        this.status = request.getStatus();
        this.requestedAt = request.getRequestedAt();
    }

    public BaseSpecRequestResponseDto(BaseSpecRequest request, User sellerWithInfo) {
        this.requestId = request.getId();
        this.sellerName = sellerWithInfo.getSellerInfo().getCompanyName();
        this.requestedModelName = request.getRequestedModelName();
        this.category = request.getCategory();
        this.manufacturer = request.getManufacturer();
        this.status = request.getStatus();
        this.requestedAt = request.getRequestedAt();
    }
}