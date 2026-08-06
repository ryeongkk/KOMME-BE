CREATE TABLE course (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    region_name VARCHAR(100) NOT NULL,
    area_code VARCHAR(10) NOT NULL,
    sigungu_code VARCHAR(10) NOT NULL,
    visit_date DATE NOT NULL,
    CONSTRAINT pk_course PRIMARY KEY (id),
    CONSTRAINT fk_course_user FOREIGN KEY (user_id) REFERENCES `user` (id)
) ENGINE = InnoDB;

CREATE TABLE course_topic (
    course_id BIGINT NOT NULL,
    topic ENUM ('FOOD', 'HEALING', 'EXPLORATION') NOT NULL,
    CONSTRAINT pk_course_topic PRIMARY KEY (course_id, topic),
    CONSTRAINT fk_course_topic_course FOREIGN KEY (course_id) REFERENCES course (id)
) ENGINE = InnoDB;
