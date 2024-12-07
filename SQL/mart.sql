drop database if exists mart;
create database mart character set utf8;
use mart;

CREATE TABLE mart_phone_price (
    id INT PRIMARY KEY AUTO_INCREMENT,
    date DATE,
    NAME NVARCHAR(255),
    trademark NVARCHAR(255),
    avg_price BIGINT,
    max_price BIGINT,
    min_price BIGINT,
    create_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL,
    create_by NVARCHAR(255),
    update_by NVARCHAR(255)
);