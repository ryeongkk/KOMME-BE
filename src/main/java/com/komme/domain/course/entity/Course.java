package com.komme.domain.course.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.komme.common.base.BaseEntity;
import com.komme.domain.course.enums.Topic;
import com.komme.domain.user.entity.User;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 사용자가 생성한 하루 코스 - Upcoming/History는 컬럼이 아니라 visitDate 기준으로 조회 시점에 계산한다
@Getter
@Entity
@Table(name = "course")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Course extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(name = "region_name", nullable = false, length = 100)
    private String regionName;

    @Column(name = "area_code", nullable = false, length = 10)
    private String areaCode;

    @Column(name = "sigungu_code", nullable = false, length = 10)
    private String sigunguCode;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "course_topic",
            joinColumns = @JoinColumn(name = "course_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "topic", nullable = false, length = 20)
    private Set<Topic> topics = new HashSet<>();

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    // 코스 엔티티 생성 - title은 LLM 생성 결과 또는 폴백("{지역명} {주제} Day")이 항상 채워진 채로 들어온다
    private Course(
            User user,
            String title,
            String description,
            String regionName,
            String areaCode,
            String sigunguCode,
            Set<Topic> topics,
            LocalDate visitDate
    ) {
        this.user = user;
        this.title = title;
        this.description = description;
        this.regionName = regionName;
        this.areaCode = areaCode;
        this.sigunguCode = sigunguCode;
        this.topics = new HashSet<>(topics);
        this.visitDate = visitDate;
    }

    // 코스 엔티티 생성 기능
    public static Course create(
            User user,
            String title,
            String description,
            String regionName,
            String areaCode,
            String sigunguCode,
            Set<Topic> topics,
            LocalDate visitDate
    ) {
        return new Course(user, title, description, regionName, areaCode, sigunguCode, topics, visitDate);
    }

    // LLM 생성 결과로 title/description 갱신 기능 (non-blocking 파이프라인에서 폴백 title로 먼저 저장한 뒤 비동기로 덮어쓸 때 사용)
    public void updateGeneratedContent(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
