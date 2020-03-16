-- !Ups

insert into "store" ("name") values ('Färm');
insert into "store" ("name") values ('BiOk');

-- !Downs

delete from "store" where "name" = 'Färm';
delete from "store" where "name" = 'BiOk';

