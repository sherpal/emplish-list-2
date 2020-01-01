
-- !Ups

drop table ingredients_in_store;
drop table ingredient_recipe;
drop table recipe;
drop table ingredient;
drop table "store";

create table ingredient (
    unique_id INTEGER primary key autoincrement,
    name varchar(255) unique not null,
    unit_name varchar(255) references unit (name)
);

create table recipe (
    unique_id INTEGER primary key autoincrement,
    name varchar(255) unique not null,
    created_by varchar(255) references users (name),
    created_on INTEGER,
    last_update_by varchar(255) references users (name),
    last_update_on INTEGER,
    description text not null
);

create table ingredient_recipe (
    ingredient_id INTEGER not null references ingredient (unique_id),
    recipe_id INTEGER not null references recipe (unique_id),
    amount real
);

create table "store" (
    unique_id INTEGER primary key autoincrement,
    name varchar(255) unique not null
);

create table ingredients_in_store (
    ingredient_id INTEGER references ingredient (unique_id),
    store_id INTEGER references "store" (unique_id)
);


