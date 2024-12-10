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
DROP PROCEDURE IF EXISTS transform_phone_data;
DELIMITER $$

CREATE PROCEDURE transform_phone_data()
BEGIN
    -- Chèn dữ liệu từ bảng origin vào bảng daily, xử lý giá trị NULL và cập nhật các cột nếu id trùng
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
        COALESCE(name, 'Unknown'),                          -- Gắn giá trị mặc định 'Unknown' cho name
        COALESCE(CAST(price AS INT), 0),                    -- Gắn giá trị mặc định 0 cho price
        COALESCE(processor, 'Unknown'),                     -- Gắn giá trị mặc định 'Unknown' cho processor
        COALESCE(CAST(capacity AS INT), 0),                 -- Gắn giá trị mặc định 0 cho capacity
        COALESCE(CAST(ram AS INT), 0),                      -- Gắn giá trị mặc định 0 cho ram
        COALESCE(CAST(screen_size AS FLOAT), 0.0),          -- Gắn giá trị mặc định 0.0 cho screen_size
        COALESCE(trade_mark, 'Unknown'),                    -- Gắn giá trị mặc định 'Unknown' cho trade_mark
        COALESCE(source, 'Unknown')                         -- Gắn giá trị mặc định 'Unknown' cho source
    FROM phone_price_daily_origin
    ON DUPLICATE KEY UPDATE
        name = VALUES(name),                
        price = VALUES(price),             
        processor = VALUES(processor),       
        capacity = VALUES(capacity),         
        ram = VALUES(ram),                   
        screen_size = VALUES(screen_size),   
        trade_mark = VALUES(trade_mark),  
        source = VALUES(source);           
END$$
DELIMITER ;

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
DROP PROCEDURE IF EXISTS TransformPhone;
CREATE PROCEDURE TransformPhone()
BEGIN
 DROP TEMPORARY TABLE IF EXISTS TempPhone;
    -- Tạo bảng tạm để chứa dữ liệu riêng biệt của Phone
    CREATE TEMPORARY TABLE TempPhone (	
         name VARCHAR(255) ,
				 processor VARCHAR(255),
         capacity INT,
         ram INT,
         screen_size FLOAT,
         trade_mark VARCHAR(255)
    );

    -- Chèn dữ liệu riêng biệt từ bảng `phone_price_daily` vào bảng tạm
    INSERT INTO TempPhone
    SELECT DISTINCT
        name,
        processor,
        capacity,
        ram,
        screen_size,
        trade_mark
    FROM phone_price_daily;

    -- Chèn dữ liệu mới vào `phone_dim` nếu chưa tồn tại
    INSERT INTO warehouse.phone_dim (
        NAME, processor, capacity, ram, screen_size, trademark, dt_expired
    )
    SELECT
        name,
        processor,
        capacity,
        ram,
        screen_size,
        trade_mark,
        NULL -- Giá trị mặc định cho `dt_expired`
    FROM TempPhone
    WHERE NOT EXISTS (
        SELECT 1
        FROM warehouse.phone_dim AS dim
        WHERE dim.NAME = TempPhone.name
          AND dim.processor = TempPhone.processor
          AND dim.capacity = TempPhone.capacity
          AND dim.ram = TempPhone.ram
          AND dim.screen_size = TempPhone.screen_size
          AND dim.trademark = TempPhone.trade_mark
    );

    -- Thêm cột `_phone` vào bảng `phone_price_daily` nếu chưa tồn tại
    ALTER TABLE phone_price_daily ADD COLUMN IF NOT EXISTS _phone INT;

    -- Cập nhật giá trị `_phone` trong bảng `phone_price_daily`
    UPDATE phone_price_daily
    JOIN warehouse.phone_dim AS dim
    ON phone_price_daily.name = dim.NAME
       AND phone_price_daily.processor = dim.processor
       AND phone_price_daily.capacity = dim.capacity
       AND phone_price_daily.ram = dim.ram
       AND phone_price_daily.screen_size = dim.screen_size
       AND phone_price_daily.trade_mark = dim.trademark
    SET phone_price_daily._phone = dim.id_phone;

    -- Xóa bảng tạm
    DROP TEMPORARY TABLE TempPhone;
END;






