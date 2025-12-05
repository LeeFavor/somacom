package com.kosta.somacom.controller;

import com.kosta.somacom.auth.PrincipalDetails;
import com.kosta.somacom.domain.score.UserActionType;
import com.kosta.somacom.service.UserIntentLoggingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class UserIntentLogController {

    private final UserIntentLoggingService userIntentLoggingService;

    /**
     * 프론트엔드에서 발생하는 사용자 행동을 로깅합니다.
     * @param payload 요청 본문 (예: {"baseSpecId": "base_13600k", "actionType": "LONG_VIEW"})
     */
    @PostMapping("/action")
    public ResponseEntity<Void> logFrontendAction(@RequestBody Map<String, String> payload,
                                                  @AuthenticationPrincipal PrincipalDetails principalDetails) {
        String baseSpecId = payload.get("baseSpecId");
        UserActionType actionType = UserActionType.valueOf(payload.get("actionType").toUpperCase());
        userIntentLoggingService.logAction(String.valueOf(principalDetails.getUser().getId()), baseSpecId, actionType);
        return ResponseEntity.ok().build();
    }
}