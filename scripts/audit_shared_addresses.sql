-- Query to detect shared addresses (Farms sharing the same address_id)
-- Should return NO ROWS if the migration V11 was successful.

SELECT 
    c.address_id, 
    COUNT(c.id) as farm_count,
    STRING_AGG(c.name, ', ') as farm_names
FROM capril c
WHERE c.address_id IS NOT NULL
GROUP BY c.address_id
HAVING COUNT(c.id) > 1;
