
-- !Ups

insert into "store" (name) values ('Colruyt'), ('Delhaize'), ('Brico');

-- !Downs

delete from "store" where name in ('Colruyt', 'Delhaize', 'Brico');


