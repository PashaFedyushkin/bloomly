create sequence portfolio_id_seq;

create table portfolio (
    id numeric not null primary key,
    description varchar(2000),
    master_id numeric not null,
    constraint fk_portfolio_master foreign key (master_id) references master (id),
    constraint uk_portfolio_master unique (master_id)
);

create table portfolio_photo (
    portfolio_id numeric not null,
    object_key varchar(500) not null,
    photo_order integer not null,
    constraint pk_portfolio_photo primary key (portfolio_id, photo_order),
    constraint fk_portfolio_photo_portfolio foreign key (portfolio_id) references portfolio (id) on delete cascade
);
