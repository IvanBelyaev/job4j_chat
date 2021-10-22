CREATE TABLE IF NOT EXISTS message (
	id serial PRIMARY KEY,
	text varchar(2000) NOT NULL,
	created timestamp NOT NULL,
	room_id integer NOT NULL,
	author_id integer NOT NULL
);

CREATE TABLE IF NOT EXISTS room (
	id serial PRIMARY KEY,
	name varchar(200) UNIQUE NOT NULL,
	created timestamp NOT NULL,
	author_id integer NOT NULL
);

CREATE TABLE IF NOT EXISTS person (
	id serial PRIMARY KEY,
	name varchar(200) UNIQUE NOT NULL,
	created timestamp NOT NULL,
	role_id integer NOT NULL
);

CREATE TABLE IF NOT EXISTS role (
	id serial PRIMARY KEY,
	name varchar(200) UNIQUE NOT NULL
);

INSERT INTO role (name) values ('USER');
