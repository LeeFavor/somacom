package com.kosta.somacom.domain.request;

import com.kosta.somacom.domain.part.PartCategory;
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
    private PartCategory category;

    @Column(nullable = false)
    private String manufacturer;

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
    public BaseSpecRequest(User seller, String requestedModelName, PartCategory category, String manufacturer, BaseSpecRequestStatus status, String adminNotes, LocalDateTime processedAt) {
        this.seller = seller;
        this.requestedModelName = requestedModelName;
        this.category = category;
        this.manufacturer = manufacturer;
        this.status = status;
        this.adminNotes = adminNotes;
        this.processedAt = processedAt;
    }

    //== 비즈니스 로직 ==//
    public void process(BaseSpecRequestStatus newStatus, String adminNotes, LocalDateTime processedAt) {
        this.status = newStatus;
        this.adminNotes = adminNotes;
        this.processedAt = processedAt;
    }
}