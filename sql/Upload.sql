drop database if exists workshop26amazons3;

create database workshop26amazons3;

use workshop26amazons3;

create table upload (
    poster_id int auto_increment not null,
    poster_name varchar(64),
    file_name varchar(64),
    mediatype varchar(256),
    short_note varchar(256),
    photo mediumblob,
    file_size bigint,
    upload_timestamp varchar(64),
    primary key(poster_id)
);