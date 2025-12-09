package com.kosta.somacom.repository;

import com.kosta.somacom.domain.request.BaseSpecRequest;
import com.kosta.somacom.domain.request.BaseSpecRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BaseSpecRequestRepository extends JpaRepository<BaseSpecRequest, Long> {
    @Query(value = "SELECT DISTINCT bsr FROM BaseSpecRequest bsr JOIN FETCH bsr.seller s WHERE bsr.status = 'PENDING' ORDER BY bsr.requestedAt ASC",
           countQuery = "SELECT count(bsr) FROM BaseSpecRequest bsr WHERE bsr.status = 'PENDING'")
    Page<BaseSpecRequest> findPendingRequestsWithSeller(Pageable pageable);
}