
-- !Ups

alter table recipe add column created_by varchar(255) references users ("name");
alter table recipe add column created_on INTEGER;
alter table recipe add column last_update_by varchar(255) references users ("name");
alter table recipe add column last_update_on INTEGER;


-- !Downs

alter table recipe drop column created_by;
alter table recipe drop column created_on;
alter table recipe drop column last_update_by;
alter table recipe drop column last_update_on;
