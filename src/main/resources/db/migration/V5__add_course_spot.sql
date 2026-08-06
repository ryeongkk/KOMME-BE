CREATE TABLE course_spot (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    course_id BIGINT NOT NULL,
    spot_id BIGINT NOT NULL,
    sequence INT NOT NULL,
    time_slot VARCHAR(20) NOT NULL,
    distance_to_next_meters INT,
    CONSTRAINT pk_course_spot PRIMARY KEY (id),
    CONSTRAINT uk_course_spot_course_sequence UNIQUE (course_id, sequence),
    CONSTRAINT fk_course_spot_course FOREIGN KEY (course_id) REFERENCES course (id),
    CONSTRAINT fk_course_spot_spot FOREIGN KEY (spot_id) REFERENCES spot (id)
) ENGINE = InnoDB;
