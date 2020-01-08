
-- !Ups

create table "store" (
    "name" varchar(255) primary key unique not null
);

create table ingredients_in_store (
    ingredient varchar(255) references ingredient ("name"),
    "store" varchar(255) references "store" ("name")
);


-- !Downs

drop table "store";
drop table ingredients_in_store;

