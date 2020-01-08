
-- !Ups

create table unit (
    "name" varchar(255) primary key unique not null
);

create table ingredient (
    "name" varchar(255) primary key unique not null,
    unit_name varchar(255) references unit (name)
);

create table recipe (
    "name" varchar(255) primary key unique not null
);

create table ingredient_recipe (
    "name" varchar(255) not null references ingredient (name),
    recipe_name varchar(255) not null references recipe (name),
    amount real
);

-- !Downs

drop table ingredient_recipe;
drop table recipe;
drop table ingredient;
drop table unit;

