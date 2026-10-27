create sequence work_interval_id_seq;

create table work_interval (
    id numeric not null primary key,
    master_id numeric not null,
    start_time time not null,
    end_time time not null,
    constraint fk_work_interval_master foreign key (master_id) references master (id) on delete cascade
);
