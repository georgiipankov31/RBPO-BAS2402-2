\getenv db_name DB_NAME
\getenv db_user DB_USER
\getenv db_password DB_PASSWORD

CREATE ROLE :"db_user" LOGIN PASSWORD :'db_password'
    NOSUPERUSER NOCREATEDB NOCREATEROLE NOREPLICATION;
CREATE DATABASE :"db_name";
REVOKE ALL ON DATABASE :"db_name" FROM PUBLIC;
GRANT CONNECT ON DATABASE :"db_name" TO :"db_user";
