
-- !Ups

alter table ingredient add column tags varchar(255) default '';


-- !Downs

alter table ingredient drop column tags;
