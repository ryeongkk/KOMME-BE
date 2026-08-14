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

// 생성된 하루 코스 - 제목은 없다. user는 "생성자"일 뿐이며, 저장 전 코스에 대한 소유권 체크에만 쓰인다.
// 실제로 "내 코스 목록"에 뜨는지는 UserCourse(저장) row의 존재 여부로 결정된다.
// Upcoming/History는 컬럼이 아니라 UserCourse 조회 시점에 visitDate 기준으로 계산한다
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

    // 코스 엔티티 생성
    private Course(
            User user,
            String regionName,
            String areaCode,
            String sigunguCode,
            Set<Topic> topics,
            LocalDate visitDate
    ) {
        this.user = user;
        this.regionName = regionName;
        this.areaCode = areaCode;
        this.sigunguCode = sigunguCode;
        this.topics = new HashSet<>(topics);
        this.visitDate = visitDate;
    }

    // 코스 엔티티 생성 기능
    public static Course create(
            User user,
            String regionName,
            String areaCode,
            String sigunguCode,
            Set<Topic> topics,
            LocalDate visitDate
    ) {
        return new Course(user, regionName, areaCode, sigunguCode, topics, visitDate);
    }
}
