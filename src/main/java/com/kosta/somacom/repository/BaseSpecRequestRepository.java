package com.kosta.somacom.repository;

import com.kosta.somacom.domain.request.BaseSpecRequest;
import com.kosta.somacom.domain.request.BaseSpecRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BaseSpecRequestRepository extends JpaRepository<BaseSpecRequest, Long> {
    @Query("SELECT DISTINCT bsr FROM BaseSpecRequest bsr JOIN FETCH bsr.seller s WHERE bsr.status = 'PENDING' ORDER BY bsr.requestedAt ASC")
    List<BaseSpecRequest> findPendingRequestsWithSeller();
}