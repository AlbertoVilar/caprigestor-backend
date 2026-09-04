alter table inventory_movement
    add column if not exists freight_cost numeric(14, 2),
    add column if not exists discount_amount numeric(14, 2);

update inventory_movement
set freight_cost = coalesce(freight_cost, 0),
    discount_amount = coalesce(discount_amount, 0)
where type = 'IN'
  and total_cost is not null;

alter table inventory_movement
    drop constraint if exists ck_inventory_movement_freight_cost_non_negative,
    drop constraint if exists ck_inventory_movement_discount_amount_non_negative;

alter table inventory_movement
    add constraint ck_inventory_movement_freight_cost_non_negative
        check (freight_cost is null or freight_cost >= 0),
    add constraint ck_inventory_movement_discount_amount_non_negative
        check (discount_amount is null or discount_amount >= 0);
