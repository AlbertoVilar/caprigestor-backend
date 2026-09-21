create table animal_sale_reversal (
    id bigserial primary key,
    sale_id bigint not null,
    reason varchar(500) not null,
    reversed_at timestamp not null,
    reversed_by bigint,
    constraint uk_animal_sale_reversal_sale unique (sale_id),
    constraint fk_animal_sale_reversal_sale foreign key (sale_id) references animal_sale(id)
);
