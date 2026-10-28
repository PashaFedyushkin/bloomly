alter table portfolio
    add column photo_key varchar(500);

update portfolio p
set photo_key = first_photo.object_key
from (
    select distinct on (portfolio_id) portfolio_id, object_key
    from portfolio_photo
    order by portfolio_id, photo_order
) first_photo
where first_photo.portfolio_id = p.id;

insert into portfolio (id, description, master_id, photo_key)
select nextval('portfolio_id_seq'), p.description, p.master_id, pp.object_key
from portfolio_photo pp
join portfolio p on p.id = pp.portfolio_id
join (
    select portfolio_id, min(photo_order) as first_order
    from portfolio_photo
    group by portfolio_id
) first_photo on first_photo.portfolio_id = pp.portfolio_id
where pp.photo_order <> first_photo.first_order;

alter table portfolio
    drop constraint uk_portfolio_master;

drop table portfolio_photo;
