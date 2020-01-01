

-- !Ups

create table users (
    "id" varchar(255) unique primary key not null,
    name varchar(255) unique not null,
    password varchar(255)
);

-- !Downs

drop table users;

