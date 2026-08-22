create sequence service_id_seq;

create table service (
    id numeric not null primary key,
    name varchar(200),
    description varchar(2000),
    price numeric,
    master_id numeric,
    constraint fk_service_master foreign key (master_id) references master (id)
);

create sequence session_id_seq;

create table session (
    id numeric not null primary key,
    date timestamp(6),
    final_price numeric,
    master_id numeric,
    service_id numeric,
    constraint fk_session_master foreign key (master_id) references master (id),
    constraint fk_session_service foreign key (service_id) references service (id)
);
