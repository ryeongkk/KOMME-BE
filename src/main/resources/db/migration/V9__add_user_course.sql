CREATE TABLE user_course (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    CONSTRAINT pk_user_course PRIMARY KEY (id),
    CONSTRAINT uk_user_course_user_course UNIQUE (user_id, course_id),
    CONSTRAINT fk_user_course_user FOREIGN KEY (user_id) REFERENCES `user` (id),
    CONSTRAINT fk_user_course_course FOREIGN KEY (course_id) REFERENCES course (id)
) ENGINE = InnoDB;
