DROP SCHEMA IF EXISTS user_accounts;

CREATE SCHEMA user_accounts;

USE user_accounts;

CREATE TABLE roles (
  role_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  role_name VARCHAR(50) NOT NULL,

  PRIMARY KEY (role_id)
);

INSERT INTO roles (role_name) values('ADMIN');
INSERT INTO roles (role_name) values('USER');

/*CREATE TABLE permissions (
  perm_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  perm_desc VARCHAR(50) NOT NULL,

  PRIMARY KEY (perm_id)
);

CREATE TABLE role_perm (
  role_id INTEGER UNSIGNED NOT NULL,
  perm_id INTEGER UNSIGNED NOT NULL,

  FOREIGN KEY (role_id) REFERENCES roles(role_id),
  FOREIGN KEY (perm_id) REFERENCES permissions(perm_id)
);*/

CREATE TABLE users(
	user_id INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
	user_name VARCHAR(50) NOT NULL UNIQUE,
	passwd VARCHAR(50) NOT NULL,
	PRIMARY KEY (user_id)
);

INSERT INTO users ( user_name, passwd ) values ('admin','admin');
INSERT INTO users ( user_name, passwd ) values ('user','user');

CREATE TABLE user_role (
  user_id INTEGER UNSIGNED NOT NULL,
  role_id INTEGER UNSIGNED NOT NULL,

  FOREIGN KEY (user_id) REFERENCES users(user_id),
  FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

INSERT INTO user_role (user_id, role_id) values(1,1);
INSERT INTO user_role (user_id, role_id) values(2,2);
