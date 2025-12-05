package com.kosta.somacom.repository.part;

import com.kosta.somacom.domain.part.GpuSpec;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GpuSpecRepository extends JpaRepository<GpuSpec, String> {
}