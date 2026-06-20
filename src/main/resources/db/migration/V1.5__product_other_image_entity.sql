-- =============================================================================
-- V1.5 : Promote product_other_image from element-collection to full entity
-- =============================================================================

DROP TABLE IF EXISTS PRODUCT_OTHER_IMAGE;

CREATE SEQUENCE PRODUCT_OTHER_IMAGE_SEQ START WITH 1 INCREMENT BY 50;

CREATE TABLE PRODUCT_OTHER_IMAGE
(
    ID         BIGINT       NOT NULL DEFAULT NEXTVAL('product_other_image_seq'),
    PRODUCT_ID BIGINT       NOT NULL,
    ALT_TEXT   VARCHAR(255),
    IMAGE_DATA TEXT         NOT NULL,
    CONSTRAINT PK_PRODUCT_OTHER_IMAGE PRIMARY KEY (ID),
    CONSTRAINT FK_PRODUCT_OTHER_IMAGE_PRODUCT FOREIGN KEY (PRODUCT_ID) REFERENCES PRODUCT (ID) ON DELETE CASCADE
);
