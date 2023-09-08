-- Mysql 8 ou superior
-- docker run --name some-mysql -d  -p 3306:3306 -e MYSQL_ROOT_PASSWORD=my-secret-pw  mysql

create database TESTDB;
use TESTDB;

create table CHANNEL (
    id binary(16) default (uuid_to_bin(uuid())) not null primary key,
    name VARCHAR(256),
    type int,
    hidden BOOLEAN
);

create table USER (
    id binary(16) default (uuid_to_bin(uuid())) not null primary key,
    pii_content_link VARCHAR(256) not null,
    suspended BOOLEAN
);

create table USER_CHANNEL (
    user_id binary(16) not null,
    channel_id binary(16) not null,
    suspended BOOLEAN default (false),
    primary key (user_id, channel_id)
);

create table MESSAGE (
    id binary(16) default (uuid_to_bin(uuid())) not null primary key,
    author binary(16),
    title varchar(256),
    content text,
    channel_id binary(16),
    CREATED_TIME TIMESTAMP
);

insert into CHANNEL (id, name, type, hidden) values (UUID_TO_BIN('347047f3-4bf4-11ee-a0e1-0242ac110002'), 'CANDIDATE SELECTION', 3, false);
insert into CHANNEL (id, name, type, hidden) values (UUID_TO_BIN('34fbfb7f-4bf4-11ee-a0e1-0242ac110002'), 'HR team', 2, false);
insert into USER (id, pii_content_link, suspended) values (UUID_TO_BIN('162b27bf-4c0b-11ee-a0e1-0242ac110002'), 'https://user.chat.com/internal/pii/93483', false);
insert into USER (id, pii_content_link, suspended) values (UUID_TO_BIN('169cd497-4c0b-11ee-a0e1-0242ac110002'), 'https://user.chat.com/internal/pii/75676', false);

insert into USER_CHANNEL (user_id, channel_id) values (UUID_TO_BIN('162b27bf-4c0b-11ee-a0e1-0242ac110002'), UUID_TO_BIN('347047f3-4bf4-11ee-a0e1-0242ac110002'));
insert into USER_CHANNEL (user_id, channel_id) values (UUID_TO_BIN('169cd497-4c0b-11ee-a0e1-0242ac110002'), UUID_TO_BIN('34fbfb7f-4bf4-11ee-a0e1-0242ac110002'));

-- Para ler as mensagens:

select BIN_TO_UUID(ID) AS MESSAGE_ID, BIN_TO_UUID(AUTHOR) as AUTHOR, TITLE, CONTENT, BIN_TO_UUID(CHANNEL_ID) AS CHANNEL_ID, CREATED_TIME FROM MESSAGE;
