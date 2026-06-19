-- =============================================================================
-- V4 : Product code sequence
-- Dedicated sequence for auto-generated product codes ({categoryId}-{seq}).
-- =============================================================================

CREATE SEQUENCE PRODUCT_CODE_SEQ
    START WITH 1
    INCREMENT BY 1 NO MINVALUE
    NO MAXVALUE
    CACHE 1;
