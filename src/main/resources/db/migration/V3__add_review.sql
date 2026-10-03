create sequence review_id_seq;

create table review (
    id numeric not null primary key,
    text varchar(1000) not null,
    stars integer not null,
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
