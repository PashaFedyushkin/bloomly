alter table master
    add column address_id numeric,
    add constraint fk_master_address foreign key (address_id) references address (id);
