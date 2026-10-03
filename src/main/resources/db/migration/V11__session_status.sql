alter table session
    add column status varchar(50);

update session
set status = 'CREATED'
where status is null;
