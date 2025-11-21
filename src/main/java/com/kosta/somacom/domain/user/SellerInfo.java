package com.kosta.somacom.domain.user;

import javax.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seller_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SellerInfo {

    @Id
    @Column(name = "seller_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "seller_id")
    private User user;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    @Column(name = "company_number", nullable = false, unique = true)
    private String companyNumber;

    @Column(name = "phone_number", nullable = false, length = 50)
    private String phoneNumber;

    @Builder
    public SellerInfo(User user, String companyName, String companyNumber, String phoneNumber) {
        this.user = user;
        this.companyName = companyName;
        this.companyNumber = companyNumber;
        this.phoneNumber = phoneNumber;
    }
}