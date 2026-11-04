create table role (
    id numeric not null primary key,
    name varchar(200)
);

create sequence role_id_seq;

insert into role (id, name) values (nextval('role_id_seq'), 'ADMIN_ROLE');
insert into role (id, name) values (nextval('role_id_seq'), 'CLIENT_ROLE');
insert into role (id, name) values (nextval('role_id_seq'), 'MASTER_ROLE');

create table users (
    id numeric not null primary key,
    name varchar(200),
    last_name varchar(200),
    creation_date timestamp(6),
    phone varchar(100),
    photo_key varchar(500),
    vk varchar(200),
    instagram varchar(200),
    telegram varchar(200)
);

create unique index uk_users_phone on users (phone) where phone is not null;

create sequence user_id_seq;

create table user_role (
    user_id numeric not null references users (id),
    role_id numeric not null references role (id),
    constraint pk_user_role primary key (user_id, role_id)
);

create table telegram (
    id numeric not null primary key,
    phone varchar(100),
    chat_id bigint,
    display_name varchar(200)
);

create sequence telegram_id_seq;

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

create table master (
    id numeric not null primary key,
    description varchar(4000),
    unp varchar(100),
    photo_key varchar(500),
    vk varchar(200),
    instagram varchar(200),
    telegram varchar(200),
    rating real,
    address_id numeric,
    constraint fk_master_user foreign key (id) references users (id),
    constraint fk_master_address foreign key (address_id) references address (id)
);

create sequence category_id_seq;

create table category (
    id numeric not null primary key,
    name varchar(200) not null
);

create sequence service_id_seq;

create table service (
    id numeric not null primary key,
    name varchar(200),
    description varchar(2000),
    price numeric,
    duration_minutes integer,
    master_id numeric,
    category_id numeric,
    constraint fk_service_master foreign key (master_id) references master (id),
    constraint fk_service_category foreign key (category_id) references category (id)
);

create sequence session_id_seq;

create table session (
    id numeric not null primary key,
    date timestamp(6),
    final_price numeric,
    user_contact varchar(250),
    status varchar(50),
    master_id numeric,
    service_id numeric,
    user_id numeric,
    constraint fk_session_master foreign key (master_id) references master (id),
    constraint fk_session_service foreign key (service_id) references service (id),
    constraint fk_session_user foreign key (user_id) references users (id)
);

create sequence review_id_seq;

create table review (
    id numeric not null primary key,
    text varchar(1000) not null,
    stars integer not null,
    date timestamp(6),
    session_id numeric not null,
    constraint fk_review_session foreign key (session_id) references session (id),
    constraint uk_review_session unique (session_id),
    constraint chk_review_stars check (stars between 1 and 5)
);

create table review_photo (
    review_id numeric not null,
    object_key varchar(500) not null,
    photo_order integer not null,
    constraint pk_review_photo primary key (review_id, photo_order),
    constraint fk_review_photo_review foreign key (review_id) references review (id) on delete cascade
);

create sequence portfolio_id_seq;

create table portfolio (
    id numeric not null primary key,
    description varchar(2000),
    photo_key varchar(500),
    master_id numeric not null,
    favorite boolean not null default false,
    constraint fk_portfolio_master foreign key (master_id) references master (id)
);

create sequence work_day_id_seq;

create table work_day (
    id numeric not null primary key,
    master_id numeric not null,
    work_date date not null,
    constraint fk_work_day_master foreign key (master_id) references master (id) on delete cascade,
    constraint uq_work_day_master_date unique (master_id, work_date)
);

create sequence work_interval_id_seq;

create table work_interval (
    id numeric not null primary key,
    start_time time not null,
    end_time time not null,
    constraint uq_work_interval_time unique (start_time, end_time)
);

create table work_day_interval (
    work_day_id numeric not null,
    work_interval_id numeric not null,
    constraint pk_work_day_interval primary key (work_day_id, work_interval_id),
    constraint fk_work_day_interval_day foreign key (work_day_id) references work_day (id) on delete cascade,
    constraint fk_work_day_interval_interval foreign key (work_interval_id) references work_interval (id) on delete cascade
);
