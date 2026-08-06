CREATE TABLE spot (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    content_id VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    category1 VARCHAR(10),
    category2 VARCHAR(10),
    category3 VARCHAR(10),
    time_slot VARCHAR(20) NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    area_code VARCHAR(10) NOT NULL,
    sigungu_code VARCHAR(10) NOT NULL,
    thumbnail_url VARCHAR(500),
    CONSTRAINT pk_spot PRIMARY KEY (id),
    CONSTRAINT uk_spot_content_id UNIQUE (content_id)
) ENGINE = InnoDB;
