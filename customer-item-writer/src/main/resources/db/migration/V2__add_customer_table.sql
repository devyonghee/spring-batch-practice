CREATE TABLE customer
(
    id             BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    first_name     VARCHAR(45) NOT NULL,
    middle_initial VARCHAR(1)  NOT NULL,
    last_name      VARCHAR(45) NOT NULL,
    address        VARCHAR(45) NOT NULL,
    city           VARCHAR(45) NOT NULL,
    state          VARCHAR(2)  NOT NULL,
    zip_code       VARCHAR(5)  NOT NULL,
    email          VARCHAR(20) NOT NULL
);