create table role(
    id numeric not null primary key,
    name varchar(200)
);

create sequence role_id_seq;

create table users(
    id numeric not null primary key,
    name varchar(200),
    last_name varchar(200),
    creation_date timestamp(6),
    phone varchar(100)
);

create unique index uk_users_phone on users (phone) where phone is not null;

create sequence user_id_seq;

create table user_role(
    user_id numeric references users (id),
    role_id numeric references role (id)
);

alter table user_role
    add constraint pk_user_role primary key (user_id, role_id);

insert into role(id, name) values (nextval('role_id_seq'), 'ADMIN_ROLE');
insert into role(id, name) values (nextval('role_id_seq'), 'CLIENT_ROLE');
insert into role(id, name) values (nextval('role_id_seq'), 'MASTER_ROLE');

create table master
(
    id  numeric not null primary key,
    unp varchar(100),
    constraint fk_master_user foreign key (id) references users (id)
);

create table telegram
(
    id  numeric not null primary key,
    phone            varchar(100),
    chat_id bigint,
    display_name varchar(200)
);

create sequence telegram_id_seq;