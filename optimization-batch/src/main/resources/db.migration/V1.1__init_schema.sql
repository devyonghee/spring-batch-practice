CREATE TABLE transaction
(
    account   BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    amount    FLOAT                 NOT NULL,
    timestamp TIMESTAMP             NOT NULL
);