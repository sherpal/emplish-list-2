
-- !Ups

alter table recipe add column tags varchar(255) default '';


-- !Downs

alter table recipe drop column tags;
