delete from work_interval;

alter table work_interval
    add column work_date date not null;
