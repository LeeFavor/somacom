package com.kosta.somacom.domain.request;

import com.kosta.somacom.domain.user.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "base_spec_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BaseSpecRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(name = "requested_model_name", nullable = false)
    private String requestedModelName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BaseSpecRequestStatus status;

    @Lob
    @Column(name = "admin_notes")
    private String adminNotes;

    @CreationTimestamp
    @Column(name = "requested_at", updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Builder
    public BaseSpecRequest(User seller, String requestedModelName, BaseSpecRequestStatus status, String adminNotes, LocalDateTime processedAt) {
        this.seller = seller;
        this.requestedModelName = requestedModelName;
        this.status = status;
        this.adminNotes = adminNotes;
        this.processedAt = processedAt;
    }
}