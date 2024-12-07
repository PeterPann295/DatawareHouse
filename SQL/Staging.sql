drop database if exists staging;
create database staging character set utf8;
use staging;

DROP TABLE if EXISTS phone_price_daily_origin;
CREATE TABLE phone_price_daily_origin (
    id INT PRIMARY KEY,
    name VARCHAR(255),
    price VARCHAR(255),
    processor VARCHAR(255),
    capacity VARCHAR(255),
    ram VARCHAR(255),
    screen_size VARCHAR(255), 
    trade_mark VARCHAR(255),
    source VARCHAR(255),
    created_at VARCHAR(255)
);
DROP TABLE if EXISTS phone_price_daily;
CREATE TABLE phone_price_daily (
    id INT PRIMARY KEY ,
    name VARCHAR(255) ,
    price VARCHAR(255),
    processor VARCHAR(255),
    capacity INT,
    ram INT,
    screen_size FLOAT,
    trade_mark VARCHAR(255),
    source VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
-- 
-- Tạo procedure tranform data từ bảng origin sang bảng daily
DROP PROCEDURE if EXISTS transform_phone_data;
CREATE PROCEDURE transform_phone_data()
BEGIN
    INSERT INTO phone_price_daily (
        id,
        name,
        price,
        processor,
        capacity,
        ram,
        screen_size,
        trade_mark,
        source
    )
    SELECT 
        id,
        name,
        CAST(price AS INT),
        processor,
        CAST(capacity AS INT),
        CAST(ram AS INT),
        CAST(screen_size AS FLOAT),
        trade_mark,
        source
    FROM phone_price_daily_origin;
END;

-- Tạo procedure TransformDate để tham chiếu khóa chính table date_dim của warehouse vào thuộc tính _date của staging
DROP PROCEDURE if EXISTS TransformDate;
CREATE PROCEDURE TransformDate()
BEGIN
		ALTER TABLE phone_price_daily ADD COLUMN if not exists _date INT;
    UPDATE phone_price_daily
    JOIN warehouse.date_dim AS dim ON CAST(phone_price_daily.created_at AS DATE) = dim.date
    SET phone_price_daily._date = dim.id_date;
END;

-- Tạo procedure TransformPhone để tham chiếu khóa chính table phone_dim của warehouse vào thuộc tính _phone của staging
DROP PROCEDURE if EXISTS TransformPhone;
CREATE PROCEDURE TransformPhone()
BEGIN
		ALTER TABLE phone_price_daily ADD COLUMN if not exists _phone INT;
    UPDATE phone_price_daily
    JOIN warehouse.phone_dim AS dim ON phone_price_daily.name = dim.NAME
    SET phone_price_daily._phone = dim.id_phone;
END;











