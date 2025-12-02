package com.kosta.somacom.admin.dto;

import com.kosta.somacom.domain.user.UserStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserStatusUpdateRequest {

    @NotNull(message = "상태 값은 비어 있을 수 없습니다.")
    private UserStatus status;
}