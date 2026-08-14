package com.komme.domain.course.entity;

import com.komme.common.base.BaseEntity;
import com.komme.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 사용자가 생성된 코스를 "저장"한 기록 - (user, course) 조합에 유니크 제약을 건다.
// 이 row가 있어야 "내 코스 목록"(다가오는 코스/지난 코스)에 노출된다. title은 저장 시점에 사용자가 직접 입력한다.
@Getter
@Entity
@Table(
        name = "user_course",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_course_user_course",
                columnNames = {"user_id", "course_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCourse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 30)
    private String title;

    // 코스 저장 엔티티 생성
    private UserCourse(User user, Course course, String title) {
        this.user = user;
        this.course = course;
        this.title = title;
    }

    // 코스 저장 엔티티 생성 기능
    public static UserCourse create(User user, Course course, String title) {
        return new UserCourse(user, course, title);
    }

    // 저장된 코스 제목 변경 기능 - 이미 저장된 코스를 다시 저장 요청하면 제목만 갱신한다
    public void changeTitle(String title) {
        this.title = title;
    }
}
