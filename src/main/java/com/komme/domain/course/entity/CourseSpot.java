package com.komme.domain.course.entity;

import com.komme.common.base.BaseEntity;
import com.komme.domain.spot.entity.Spot;
import com.komme.domain.spot.enums.TimeSlot;

import jakarta.persistence.Column;
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

// 코스 안에서 스팟 하나가 배치된 슬롯 - timeSlot은 생성 시점 Spot.timeSlot의 스냅샷이라, 이후 Spot이 바뀌어도 코스 기록은 유지된다
@Getter
@Entity
@Table(name = "course_spot")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CourseSpot extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "spot_id", nullable = false)
    private Spot spot;

    @Column(nullable = false)
    private int sequence;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_slot", nullable = false, length = 20)
    private TimeSlot timeSlot;

    @Column(name = "distance_to_next_meters")
    private Integer distanceToNextMeters;

    // 코스 스팟 엔티티 생성 - 마지막 스팟은 다음 스팟이 없으므로 distanceToNextMeters가 null일 수 있다
    private CourseSpot(Course course, Spot spot, int sequence, TimeSlot timeSlot, Integer distanceToNextMeters) {
        this.course = course;
        this.spot = spot;
        this.sequence = sequence;
        this.timeSlot = timeSlot;
        this.distanceToNextMeters = distanceToNextMeters;
    }

    // 코스 스팟 엔티티 생성 기능
    public static CourseSpot create(Course course, Spot spot, int sequence, TimeSlot timeSlot, Integer distanceToNextMeters) {
        return new CourseSpot(course, spot, sequence, timeSlot, distanceToNextMeters);
    }
}
