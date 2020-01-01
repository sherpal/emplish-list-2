
-- !Ups

alter table recipe add column for_how_many_people INTEGER default 2;


-- !Downs

alter table recipe drop column for_how_many_people;
