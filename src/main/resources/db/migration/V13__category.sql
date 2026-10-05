create sequence category_id_seq;

create table category (
    id numeric not null primary key,
    name varchar(200) not null
);

alter table service
    add column category_id numeric,
    add constraint fk_service_category foreign key (category_id) references category (id);

alter table service
    add column duration_minutes integer;
