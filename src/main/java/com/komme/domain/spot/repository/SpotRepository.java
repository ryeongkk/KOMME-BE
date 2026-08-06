package com.komme.domain.spot.repository;

import com.komme.domain.spot.entity.Spot;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpotRepository extends JpaRepository<Spot, Long> {

    // contentId 기반 스팟 조회 기능 (write-through upsert 시 기존 row 존재 여부 확인용)
    Optional<Spot> findByContentId(String contentId);
}
