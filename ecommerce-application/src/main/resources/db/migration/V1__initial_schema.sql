-- =============================================================================
-- V1 : Initial schema
-- Complete schema for the first release of the project.
-- =============================================================================

-- Hibernate allocationSize = 50, so the sequence must increment by 50.
CREATE SEQUENCE APP_USER_SEQ
    START WITH 1
    INCREMENT BY 50 NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE APP_USER
(
    ID            BIGINT       NOT NULL DEFAULT NEXTVAL('app_user_seq'),
    FIRST_NAME    VARCHAR(255) NOT NULL,
    LAST_NAME     VARCHAR(255) NOT NULL,
    USERNAME      VARCHAR(255) NOT NULL,
    PASSWORD      VARCHAR(512) NOT NULL,
    IS_REGISTERED BOOLEAN      NOT NULL,
    NATIONAL_ID   VARCHAR(20),
    EMAIL         VARCHAR(255),
    BIRTH_DATE    TIMESTAMP,
    CREATED_AT    TIMESTAMP    NOT NULL DEFAULT NOW(),
    ROLE          VARCHAR(32)  NOT NULL,
    IS_ENABLED    BOOLEAN      NOT NULL,

    CONSTRAINT PK_APP_USER PRIMARY KEY (ID),
    CONSTRAINT UK_APP_USER_USERNAME UNIQUE (USERNAME),
    CONSTRAINT UK_APP_USER_MOBILE UNIQUE (MOBILE)
);