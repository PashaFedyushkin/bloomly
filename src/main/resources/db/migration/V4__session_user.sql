alter table session
    add column user_id numeric,
    add constraint fk_session_user foreign key (user_id) references users (id);
