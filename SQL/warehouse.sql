drop database if exists warehouse;
create database warehouse character set utf8;
use warehouse;

CREATE TABLE phone_dim (
    id_phone INT PRIMARY KEY AUTO_INCREMENT,
    NAME NVARCHAR(255),
    processor NVARCHAR(255),
    capacity INT,
    ram INT,
    screen_size FLOAT,
    trademark NVARCHAR(255),
    dt_expired DATETIME
);
CREATE TABLE date_dim (
    id_date INT PRIMARY KEY AUTO_INCREMENT,
    date DATE,
    day VARCHAR(50),
    month VARCHAR(50),
    year VARCHAR(50),
    hour VARCHAR(50),
    minute VARCHAR(50)
);
-- Dữ liệu trong bảng này phản ánh giá của từng sản phẩm vào từng ngày cụ thể, giúp tạo cơ sở cho các phân tích về giá thay đổi theo thời gian.
-- Lưu trữ các sự kiện liên quan đến giá điện thoại, liên kết với bảng phone_dim qua id_phone và bảng date_dim qua id_date
CREATE TABLE phone_price_fact (
    id INT PRIMARY KEY AUTO_INCREMENT,
    id_phone INT REFERENCES phone_dim(id_phone),
    id_date INT REFERENCES date_dim(id_date),
    price INT,
    SOURCE NVARCHAR(255),
    dt_expired DATETIME
);
-- Chứa các bản ghi tổng hợp về giá điện thoại, như giá trung bình, cao nhất và thấp nhất của từng sản phẩm hoặc từng thương hiệu theo ngày, tháng hoặc năm.
-- Bảng này giúp rút gọn dữ liệu, từ đó đẩy nhanh các phân tích về xu hướng giá hoặc so sánh giá trung bình giữa các thương hiệu.
CREATE TABLE phone_price_aggregate (
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

-- Tạo procedure SetDataExpired để cập nhật hạn sử dụng của dữ liệu = thời gian hiện tại 
DELIMITER $$
drop procedure if exists SetDataExpired;
create procedure SetDataExpired()
BEGIN
	UPDATE phone_price_fact
  SET dt_expired  = CURRENT_TIMESTAMP;
END$$
DELIMITER;
