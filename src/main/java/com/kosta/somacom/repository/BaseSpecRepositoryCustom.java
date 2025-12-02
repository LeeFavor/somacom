package com.kosta.somacom.repository;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.dto.request.BaseSpecSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BaseSpecRepositoryCustom {

    Page<BaseSpec> searchBaseSpecs(BaseSpecSearchCondition condition, Pageable pageable);
}