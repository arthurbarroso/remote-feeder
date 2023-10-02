CREATE TABLE IF NOT EXISTS users (
			 uid text NOT NULL PRIMARY KEY,
			 username varchar(30)  UNIQUE NOT NULL,
			 password TEXT NOT NULL
);
