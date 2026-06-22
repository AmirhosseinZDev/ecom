-- =============================================================================
-- V1.7 : Drop the cart aggregate; key cart items directly by user.
-- A user's cart is simply the set of cart_item rows owned by that user; there is
-- no separate cart row. cart_item is empty at this point (introduced in V1.6),
-- so the column reshape is safe.
-- =============================================================================

ALTER TABLE CART_ITEM DROP CONSTRAINT IF EXISTS UK_CART_ITEM_PRODUCT_VARIANT;
ALTER TABLE CART_ITEM DROP CONSTRAINT IF EXISTS FK_CART_ITEM_CART;
ALTER TABLE CART_ITEM DROP COLUMN IF EXISTS CART_ID;

ALTER TABLE CART_ITEM ADD COLUMN USER_ID BIGINT NOT NULL;

ALTER TABLE CART_ITEM
    ADD CONSTRAINT FK_CART_ITEM_USER FOREIGN KEY (USER_ID) REFERENCES APP_USER (ID) ON DELETE CASCADE;
ALTER TABLE CART_ITEM
    ADD CONSTRAINT UK_CART_ITEM_USER_PRODUCT_VARIANT UNIQUE (USER_ID, PRODUCT_ID, VARIANT_TYPE);

DROP TABLE IF EXISTS CART;
DROP SEQUENCE IF EXISTS CART_SEQ;
