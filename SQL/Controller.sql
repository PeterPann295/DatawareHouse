DROP DATABASE IF EXISTS controller;
CREATE DATABASE controller character set utf8;
USE controller;

DROP TABLE IF EXISTS config_table;
CREATE TABLE config_tables(
                              id INT PRIMARY KEY AUTO_INCREMENT,
                              name_tb_staging_origin VARCHAR(250),
                              name_tb_staging_transformed VARCHAR(250),
                              name_tb_warehouse_phone_dim VARCHAR(250),
                              name_tb_warehouse_date_dim VARCHAR(250),
                              name_tb_warehouse_phone_fact VARCHAR(250),
                              name_tb_warehouse_aggregate VARCHAR(250),
                              name_tb_mart VARCHAR(250)
);
INSERT INTO config_tables VALUES(1, 'phone_price_daily_origin', 'phone_price_daily', 'phone_dim','date_dim', 'phone_price_fact', 'phone_price_aggregate', 'mart_phone_price');
DROP TABLE IF EXISTS config_files;
CREATE TABLE config_files (
                              id INT PRIMARY KEY AUTO_INCREMENT,
                              author VARCHAR(255) DEFAULT NULL,
                              email VARCHAR(255) DEFAULT NULL,
                              source VARCHAR(255) NOT NULL,
                              directory_file VARCHAR(510) DEFAULT 'D:\\',
                              id_config_table INT,
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                              updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                              CONSTRAINT fk_config_table FOREIGN KEY (id_config_table) REFERENCES config_tables(id)
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

INSERT INTO config_files (`id`, `author`, `email`, `source`, `directory_file`, `created_at`, `updated_at`) VALUES
	(1, 'Minh Hieu', 'leminhhieu.ltp2021@gmail.com', 'fptshop.com.vn', 'D:\\DataWarehouse\\', '2024-11-27 11:39:34', '2024-11-27 22:51:57');

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
    id_phone,
    id_date,
    price,
    source
  )
  SELECT 
    daily._phone, 
    daily._date, 
    CAST(daily.price AS int), 
    daily.source
  FROM 
    staging.phone_price_daily AS daily;
END $$

DELIMITER ;


DELIMITER $$

CREATE PROCEDURE load_to_aggregate()
BEGIN
    INSERT INTO warehouse.phone_price_aggregate (
        date, 
        NAME, 
        trademark, 
        avg_price, 
        max_price, 
        min_price,
        create_at,
        updated_at,
        create_by,
        update_by
    )
    SELECT 
        d.date,                    
        p.name,                    
        p.trademark,               
        AVG(f.price) AS avg_price, 
        MAX(f.price) AS max_price, 
        MIN(f.price) AS min_price,
        CURRENT_TIMESTAMP AS create_at, 
        CURRENT_TIMESTAMP AS updated_at, -- Gán giá trị thời gian hiện tại
        'system' AS create_by,           -- Mặc định người tạo là 'system'
        'system' AS update_by            -- Mặc định người cập nhật là 'system'
    FROM 
        warehouse.phone_price_fact f
    JOIN 
        warehouse.phone_dim p ON f.id_phone = p.id_phone
    JOIN 
        warehouse.date_dim d ON f.id_date = d.id_date
    GROUP BY 
        d.date, p.name, p.trademark;    
END $$

DELIMITER ;

