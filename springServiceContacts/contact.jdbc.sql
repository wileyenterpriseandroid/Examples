CREATE TABLE contact
(
id int NOT NULL AUTO_INCREMENT,
firstName varchar(255) NOT NULL,
lastName varchar(255) NOT NULL,
phone varchar(255),
email varchar(255),
deleted boolean,
updateTime bigint(20) NOT NULL,
version bigint(20) NOT NULL,
PRIMARY KEY (id)
);

create index updateTimeIndex on contact (updateTime);
create index firstNameIndex on contact (firstName);
