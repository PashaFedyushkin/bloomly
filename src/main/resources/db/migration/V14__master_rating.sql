alter table master
    add column rating real;

update master m
set rating = sub.avg_stars
from (
    select s.master_id, avg(r.stars)::real as avg_stars
    from review r
    join session s on s.id = r.session_id
    where s.master_id is not null
    group by s.master_id
) sub
where m.id = sub.master_id;
