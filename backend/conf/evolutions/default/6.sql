
-- !Ups

alter table recipe add column unique_id INTEGER;


-- !Downs

alter table recipe drop column unique_id;
