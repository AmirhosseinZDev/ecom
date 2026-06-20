-- =============================================================================
-- V1.2 : Add other image columns to product
-- Splits the single image embedded field into mainImage + otherImages.
-- =============================================================================

ALTER TABLE PRODUCT
    ADD COLUMN OTHER_IMAGE_ALT_TEXT VARCHAR(255),
    ADD COLUMN OTHER_IMAGE_MEDIA_ID  BIGINT,
    ADD CONSTRAINT FK_PRODUCT_OTHER_IMAGE_MEDIA FOREIGN KEY (OTHER_IMAGE_MEDIA_ID) REFERENCES MEDIA (ID);
