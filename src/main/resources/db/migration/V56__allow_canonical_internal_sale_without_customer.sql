-- Canonical INTERNAL_SALE rows identify the destination farm and do not
-- require a commercial customer. Historical rows remain readable unchanged.
ALTER TABLE animal_sale
    ALTER COLUMN customer_id DROP NOT NULL;
