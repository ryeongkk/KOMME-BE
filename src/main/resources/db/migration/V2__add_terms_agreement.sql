CREATE TABLE terms_agreement (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    user_id BIGINT NOT NULL,
    terms_type VARCHAR(30) NOT NULL,
    agreed BOOLEAN NOT NULL,
    agreed_at DATETIME(6),
    CONSTRAINT pk_terms_agreement PRIMARY KEY (id),
    CONSTRAINT uk_terms_agreement_user_type UNIQUE (user_id, terms_type),
    CONSTRAINT fk_terms_agreement_user FOREIGN KEY (user_id) REFERENCES `user` (id)
) ENGINE = InnoDB;
