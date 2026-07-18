CREATE TABLE `user` (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255),
    nickname VARCHAR(20),
    provider ENUM ('LOCAL', 'GOOGLE', 'APPLE') NOT NULL,
    nationality VARCHAR(2),
    gender ENUM ('MALE', 'FEMALE'),
    preferred_language ENUM ('ENGLISH', 'JAPANESE', 'CHINESE_SIMPLIFIED'),
    CONSTRAINT pk_user PRIMARY KEY (id),
    CONSTRAINT uk_user_email UNIQUE (email),
    CONSTRAINT uk_user_nickname UNIQUE (nickname)
) ENGINE = InnoDB;

CREATE TABLE oauth_account (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    user_id BIGINT NOT NULL,
    provider ENUM ('LOCAL', 'GOOGLE', 'APPLE') NOT NULL,
    provider_id VARCHAR(255) NOT NULL,
    CONSTRAINT pk_oauth_account PRIMARY KEY (id),
    CONSTRAINT uk_oauth_account_provider_id UNIQUE (provider, provider_id),
    CONSTRAINT fk_oauth_account_user FOREIGN KEY (user_id) REFERENCES `user` (id)
) ENGINE = InnoDB;

CREATE TABLE user_service_interest (
    user_id BIGINT NOT NULL,
    service_interest ENUM ('COURSE', 'TOURIST_SPOT', 'CULTURE', 'FOOD', 'SHOPPING') NOT NULL,
    CONSTRAINT pk_user_service_interest PRIMARY KEY (user_id, service_interest),
    CONSTRAINT fk_user_service_interest_user FOREIGN KEY (user_id) REFERENCES `user` (id)
) ENGINE = InnoDB;
