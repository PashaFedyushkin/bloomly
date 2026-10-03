create sequence address_id_seq;

create table address (
    id numeric not null primary key,
    country varchar(200),
    region varchar(200),
    locality varchar(200),
    district varchar(200),
    place varchar(200),
    street varchar(200),
    house varchar(50),
    apartment varchar(50)
);
