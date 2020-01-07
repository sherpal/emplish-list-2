

-- !Ups

create table pending_registration (
    name varchar(255) unique primary key not null,
    password varchar(255),
    email varchar(255),
    random_key varchar(255)
);

-- !Downs

drop table pending_registration;

