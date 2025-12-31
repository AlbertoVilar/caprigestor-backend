-- Migration to enforce OneToOne relationship between GoatFarm (capril) and Address (endereco)
-- 1. Detect duplicates (farms sharing the same address)
-- 2. Clone shared addresses so each farm has its own unique address
-- 3. Add UNIQUE constraint to capril.address_id

DO $$
DECLARE
    r RECORD;
    new_addr_id BIGINT;
BEGIN
    -- Loop through farms that share an address, excluding the first one found (MIN(id)) for each address group
    -- This ensures one farm keeps the original address, and others get a copy.
    FOR r IN
        SELECT c.id AS farm_id, c.address_id
        FROM capril c
        JOIN (
            SELECT address_id
            FROM capril
            WHERE address_id IS NOT NULL
            GROUP BY address_id
            HAVING COUNT(*) > 1
        ) dup ON c.address_id = dup.address_id
        WHERE c.id NOT IN (
            SELECT MIN(id)
            FROM capril
            WHERE address_id IS NOT NULL
            GROUP BY address_id
            HAVING COUNT(*) > 1
        )
    LOOP
        -- Clone the address data into a new row
        -- Note: We include 'numero' and 'complemento' from the DB schema even if Java entity might not map them currently
        INSERT INTO endereco (rua, numero, complemento, bairro, cidade, estado, cep, pais, created_at, updated_at)
        SELECT rua, numero, complemento, bairro, cidade, estado, cep, pais, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
        FROM endereco
        WHERE id = r.address_id
        RETURNING id INTO new_addr_id;

        -- Update the specific farm to point to the new, unique address
        UPDATE capril SET address_id = new_addr_id WHERE id = r.farm_id;
        
        RAISE NOTICE 'Fixed shared address for farm_id %: old_address_id %, new_address_id %', r.farm_id, r.address_id, new_addr_id;
    END LOOP;
END $$;

-- Now that data is cleaned up, enforce the constraint
ALTER TABLE capril ADD CONSTRAINT uk_capril_address_id UNIQUE (address_id);
