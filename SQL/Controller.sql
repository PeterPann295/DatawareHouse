DROP DATABASE IF EXISTS controller;
CREATE DATABASE controller character set utf8;
USE controller;

DROP TABLE IF EXISTS config_files;
CREATE TABLE config_files (
    id INT PRIMARY KEY AUTO_INCREMENT,
    author VARCHAR(255) DEFAULT NULL,
    email VARCHAR(255) DEFAULT NULL,
    source VARCHAR(255) NOT NULL,
    directory_file VARCHAR(510) DEFAULT 'D:\\',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS log_files;
CREATE TABLE log_files (
    id INT PRIMARY KEY AUTO_INCREMENT,
    id_config INT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    detail_file_path VARCHAR(510) DEFAULT NULL,
    is_processing BOOLEAN DEFAULT FALSE,
    status VARCHAR(255) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_config FOREIGN KEY (id_config) REFERENCES config_files(id)
);
DROP TABLE IF EXISTS logs;
CREATE TABLE logs (
    id INT PRIMARY KEY AUTO_INCREMENT,
    id_log_file int ,
    status VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_log_file FOREIGN KEY (id_log_file) REFERENCES log_files(id)
);


drop procedure if EXISTS GetLogFiles;
DELIMITER //

CREATE PROCEDURE GetLogFiles (
    IN p_config_id INT, -- ID của config_files
    IN p_date DATE      -- Ngày cần kiểm tra
)
BEGIN
    -- Biến để lưu kết quả kiểm tra
    DECLARE record_exists BOOLEAN;

    -- Kiểm tra xem có bản ghi tồn tại không
    SELECT EXISTS (
        SELECT 1 
        FROM log_files
        WHERE id_config = p_config_id
          AND DATE(created_at) = p_date
    ) INTO record_exists;

    -- Kiểm tra kết quả và trả về dữ liệu
    IF record_exists THEN
        -- Trả về danh sách các log files
        SELECT *
        FROM log_files
        WHERE id_config = p_config_id
          AND DATE(created_at) = p_date;
    ELSE
        -- Trả về NULL nếu không có bản ghi
        SELECT 'No records found' AS message;
    END IF;
END
DELIMITER ;



drop procedure if EXISTS InsertLogFile;
DELIMITER //
CREATE PROCEDURE InsertLogFile (
    IN p_id_config_file INT,   -- ID của bảng config_files
    IN p_date VARCHAR(20),     -- Ngày (chuỗi định dạng yyyy-MM-dd)
    IN p_status VARCHAR(255)   -- Trạng thái (ví dụ: 'PENDING', 'COMPLETED')
)
BEGIN
    DECLARE v_source VARCHAR(255);
    DECLARE v_directory_file VARCHAR(510);
    DECLARE v_file_name VARCHAR(510);
    DECLARE v_detail_file_path VARCHAR(510);

    IF NOT EXISTS (SELECT 1 FROM config_files WHERE id = p_id_config_file) THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'ID config_files không tồn tại!';
    END IF;

    SELECT source, directory_file
    INTO v_source, v_directory_file
    FROM config_files
    WHERE id = p_id_config_file;

    SET v_file_name = CONCAT(p_date, '_', v_source);
    SET v_detail_file_path = CONCAT(v_directory_file, v_file_name);

    -- Chèn vào bảng log_files
    INSERT INTO log_files (
        id_config,
        file_name,
        detail_file_path,
        is_processing,
        status
    )
    VALUES (
        p_id_config_file,
        v_file_name,
        v_detail_file_path,
        FALSE,
        p_status
    );

    SELECT CONCAT('Log file "', v_file_name, '" đã được thêm thành công.') AS message;
END
DELIMITER ;





drop procedure if EXISTS InsertLog;
DELIMITER //
create procedure InsertLog(
	id_log_files int,
	status varchar(100),
	description varchar(255)
)
BEGIN
	insert into logs(id_config, status, description)
	values (id_log_file, status, description);
END
DELIMITER ;

-- Tạo procedure thay đổi status của config có id là input_id và status = input_status
DROP PROCEDURE IF EXISTS UpdateStatus;
DELIMITER //
CREATE PROCEDURE UpdateStatus(
    IN input_id INT,
    IN input_status VARCHAR(255)
)
BEGIN
    UPDATE log_files
    SET status= input_status
    WHERE id = input_id;
END
DELIMITER ;

-- Tạo procedure cập nhật giá trị của is_processing trong config có id là input_id và is_processing = input_is_processing
DROP PROCEDURE IF EXISTS UpdateIsProcessing;
DELIMITER //

CREATE PROCEDURE UpdateIsProcessing(
    IN input_id INT,
    IN input_is_processing BOOLEAN
)
BEGIN
    UPDATE log_files
    SET is_processing = input_is_processing
    WHERE id = input_id;
END //

DELIMITER ;



DROP PROCEDURE IF EXISTS truncate_staging_table;
DELIMITER //
CREATE PROCEDURE truncate_staging_table()
BEGIN
    TRUNCATE table staging.staging;
END //
DELIMITER ;

-- Tạo procedure TransformData call các procedure của database staging để transform dữ liệu 
DROP PROCEDURE IF EXISTS TransformData;
CREATE PROCEDURE TransformData()
BEGIN
		-- transform origin to daily
    call staging.transform_phone_data();
		-- transform date
		call staging.TransformDate();
		-- transform phone
		call staging.TransformPhone();
END;

-- Tạo procedure LoadDataToWH lấy các thuộc tính từ staging và các id của bảng dim của db staging load vào table fact trong database warehouse

DROP PROCEDURE IF EXISTS LoadDataToWH;
DELIMITER $$

CREATE PROCEDURE LoadDataToWH()
BEGIN
  -- Gọi procedure SetDataExpired để cập nhật giá trị dt_expired
  CALL warehouse.SetDataExpired();
  
  -- Chèn dữ liệu vào bảng phone_price_fact
  INSERT INTO warehouse.phone_price_fact(
    id,
    id_phone,
    id_date,
    price,
    source
  )
  SELECT 
    daily.id, 
    daily._phone, 
    daily._date, 
    CAST(daily.price AS int), 
    daily.source
  FROM 
    staging.phone_price_daily AS daily;
END $$

DELIMITER ;
