
-- !Ups

insert into "store" (name) values ('Colruyt'), ('Delhaize'), ('Brico');
insert into unit (name) values ('Piece'), ('g'), ('kg'), ('litre');

-- !Downs

delete from "store" where name in ('Colruyt', 'Delhaize', 'Brico');
delete from unit where name in ('Piece', 'g', 'kg', 'litre');


