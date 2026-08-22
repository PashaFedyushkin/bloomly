create table role(
    id numeric not null primary key,
    name varchar(200)
);

create sequence role_id_seq;

create table users(
    id numeric not null primary key,
    name varchar(200),
    creation_date timestamp(6),
    phone varchar(100),
    telegram_chat_id bigint
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

create table master (
                        id numeric not null primary key,
                        unp varchar(100),
                        constraint fk_master_user foreign key (id) references users (id)
);