alter table inventory_movement
    add column if not exists unit_cost numeric(14, 4),
    add column if not exists total_cost numeric(14, 2),
    add column if not exists purchase_date date,
    add column if not exists supplier_name varchar(120);

create table if not exists operational_expense (
    id bigserial primary key,
    farm_id bigint not null,
    category varchar(30) not null,
    description varchar(200) not null,
    amount numeric(12, 2) not null,
    expense_date date not null,
    notes varchar(500),
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    constraint fk_operational_expense_farm
        foreign key (farm_id) references capril (id)
);

create index if not exists idx_operational_expense_farm_date
    on operational_expense (farm_id, expense_date desc);

create index if not exists idx_inventory_movement_purchase_date
    on inventory_movement (farm_id, purchase_date desc)
    where purchase_date is not null and total_cost is not null;
