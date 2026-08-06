package com.komme.domain.spot.entity;

import java.math.BigDecimal;

import com.komme.common.base.BaseEntity;
import com.komme.domain.spot.enums.TimeSlot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 코스 안에서 방문하는 장소 하나 - tourapi 원본 응답을 contentId 기준으로 write-through 매핑한 로컬 사본
@Getter
@Entity
@Table(
        name = "spot",
        uniqueConstraints = @UniqueConstraint(name = "uk_spot_content_id", columnNames = "content_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Spot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_id", nullable = false, length = 20)
    private String contentId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 10)
    private String category1;

    @Column(length = 10)
    private String category2;

    @Column(length = 10)
    private String category3;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_slot", nullable = false, length = 20)
    private TimeSlot timeSlot;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "area_code", nullable = false, length = 10)
    private String areaCode;

    @Column(name = "sigungu_code", nullable = false, length = 10)
    private String sigunguCode;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    // 스팟 엔티티 생성 - contentId는 불변, 나머지는 Attributes로 묶어서 받음
    private Spot(String contentId, Attributes attributes) {
        this.contentId = contentId;
        apply(attributes);
    }

    // 스팟 엔티티 생성 기능
    public static Spot create(String contentId, Attributes attributes) {
        return new Spot(contentId, attributes);
    }

    // tourapi 원본 재조회 결과로 필드 갱신 기능 (write-through upsert의 update 경로, contentId는 갱신 대상 아님)
    public void refresh(Attributes attributes) {
        apply(attributes);
    }

    private void apply(Attributes attributes) {
        this.name = attributes.name();
        this.category1 = attributes.category1();
        this.category2 = attributes.category2();
        this.category3 = attributes.category3();
        this.timeSlot = attributes.timeSlot();
        this.latitude = attributes.latitude();
        this.longitude = attributes.longitude();
        this.areaCode = attributes.areaCode();
        this.sigunguCode = attributes.sigunguCode();
        this.thumbnailUrl = attributes.thumbnailUrl();
    }

    // contentId를 제외한, tourapi 원본에서 매번 새로 채워지는 필드 묶음
    public record Attributes(
            String name,
            String category1,
            String category2,
            String category3,
            TimeSlot timeSlot,
            BigDecimal latitude,
            BigDecimal longitude,
            String areaCode,
            String sigunguCode,
            String thumbnailUrl
    ) {
    }
}
