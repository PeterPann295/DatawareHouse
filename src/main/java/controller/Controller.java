package controller;

import com.opencsv.CSVWriter;
import dao.PhonePriceDao;
import entity.LogFile;
import util.FakePhonePriceDaily;
import util.SendMail;
import util.URLChecker;

import java.io.FileWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Controller {
    public void getData(Connection con, LogFile logFile){
        PhonePriceDao phonePriceDao = new PhonePriceDao();

        //7.Cập nhật  trạng thái  xử lí log_file là đang xử lý (isProcessing=true)
        phonePriceDao.updateIsProcessing(con, logFile.getId(), true);

        // 8. Cập nhật status log_file là CRAWLING
        phonePriceDao.updateStatus(con, logFile.getId(), "CRAWLING");

        // 9. Ghi Log đang tiến hành lấy dữ liệu
        phonePriceDao.insertLog(con, logFile.getId(), "CRAWLING", "Start craw data");

        //10.Lấy ra src web định lấy dữ liệu
        String src = logFile.getConfigFile().getSource();

        // 11.Kiểm tra kết nối
        if(!URLChecker.isURLActive("https://"+src)){
            // 11.1 Ghi log kết nối đến src Web thất bại
            phonePriceDao.insertLog(con, logFile.getId(), "FAILED", "Connect to source web failed");
            // 11.2 Xét trạng thái File_log: FAILED
            phonePriceDao.updateStatus(con, logFile.getId(), "FAILED");
            return;
        }
        //12.
        phonePriceDao.insertLog(con, logFile.getId(), "CRAWLING", "Connect to source web Success");

        String detailFilePath = logFile.getDetailFilePath() +".csv";

        try {
            //13. Tạo file CSV
            CSVWriter writer = new CSVWriter(new FileWriter(detailFilePath));

            String[] header = {
                    "NAME", "price", "processor", "capacity", "ram", "screen_size", "trademark", "SOURCE", "create_at"
            };
            writer.writeNext(header);

            // 14. Lấy dữ liệu và ghi vào file CSV
            List<String[]> fakeData = FakePhonePriceDaily.generateFakeData(10); // 10 bản ghi giả lập
            writer.writeAll(fakeData);

            writer.close();
            System.out.println("File CSV đã được tạo tại: " + detailFilePath);

            System.out.println("CRAWLED success");
            // 15. Xét trạng thái log_file: CRAWLED
            phonePriceDao.updateStatus(con, logFile.getId(), "CRAWLED");
            // 16. Ghi Log: Kết thúc Crawl thành công
            phonePriceDao.insertLog(con, logFile.getId(), "CRAWLED", "End crawl success, data to "+ detailFilePath);
            // 17.Cập nhật  trạng thái  xử lí log_file là đang xử lý (isProcessing=false)
            phonePriceDao.updateIsProcessing(con, logFile.getId(), false);
        } catch (Exception e) {
            // 14.1 Xét trạng thái log_file: FAILED
            phonePriceDao.updateStatus(con, logFile.getId(), "FAILED");
            // 14.2 Ghi Log: CRAWL dữ liệu thất bại
            phonePriceDao.insertLog(con, logFile.getId(), "FAILED", "CRAWl Data FAILED WITH MESSAGE "+ e.getMessage());
            // 14.3.Cập nhật  trạng thái  xử lí log_file là đang xử lý (isProcessing=false)
            phonePriceDao.updateIsProcessing(con, logFile.getId(), false);

            String mail = logFile.getConfigFile().getEmail();
            DateTimeFormatter dt = DateTimeFormatter.ofPattern("hh:mm:ss dd/MM/yyyy");
            LocalDateTime nowTime = LocalDateTime.now();
            String timeNow = nowTime.format(dt);
            String subject = "Error Date: " + timeNow;
            String message = "Error with message: "+e.getMessage();
            // 14.4 Gửi Mail đến Author
            SendMail.sendMail(mail, subject, message);
        }
    }
    public void transfromData(Connection con, LogFile logFile){
        PhonePriceDao phonePriceDao = new PhonePriceDao();
        try (CallableStatement callableStatement = con.prepareCall("{CALL TransformData()}")) {
            // Thực hiện stored procedure
            callableStatement.execute();
            //11.
            System.out.println("transform success!");
            //(12. Cập nhật status của config thành TRANSFORMED
            phonePriceDao.updateStatus(con, logFile.getId(), "TRANSFORMED");
            //13. Thêm thông tin đã transform data vào log
            phonePriceDao.insertLog(con, logFile.getId(), "", "Đã transform data");

            //14. cập nhật trạng thái xử li file_log là đang xu ly = false
            phonePriceDao.updateIsProcessing(con, logFile.getId(), false);
        } catch (SQLException e) {
            //11.1 xxet trang thai file log là fail
            phonePriceDao.updateStatus(con, logFile.getId(), "FAIlED");

            //11.2 Ghi log tranform thất bại
            phonePriceDao.insertFileLog(con, logFile.getId(), "FAIL", "Tranform data fail");
            //11.3cập nhật trạng thái xử li file_log là đang xu ly = false
            phonePriceDao.updateIsProcessing(con, logFile.getId(), false);
            // 11.4 Gửi Mail đến Author
            String mail = logFile.getConfigFile().getEmail();
            DateTimeFormatter dt = DateTimeFormatter.ofPattern("hh:mm:ss dd/MM/yyyy");
            LocalDateTime nowTime = LocalDateTime.now();
            String timeNow = nowTime.format(dt);
            String subject = "Error Date: " + timeNow;
            String message = "Error with message: "+e.getMessage();
            SendMail.sendMail(mail, subject, message);


        }

    }
    public void loadToWarehouse(Connection con, LogFile logFile) {
        PhonePriceDao phonePriceDao = new PhonePriceDao();
        try (CallableStatement callableStatement = con.prepareCall("{CALL LoadDataToWH()}")) {
            // Thực hiện stored procedure
            callableStatement.execute();
            //12
            System.out.println("load success!");
            //13. Cập nhật status của config thành WH_LOADED
            phonePriceDao.updateStatus(con, logFile.getId(), "WH_LOADED");
            //14. ghi log da load data to wh
            phonePriceDao.insertLog(con, logFile.getId(), "", "Đã load data to wh");
            //15. cập nhật trạng thái xử li file_log là đang xu ly = false
            phonePriceDao.updateIsProcessing(con, logFile.getId(), false);

        } catch (SQLException e) {
            //12.1 xxet trang thai file log là fail
            phonePriceDao.updateStatus(con, logFile.getId(), "FAIlED");

            //12.2 Ghi log load thất bại
            phonePriceDao.insertFileLog(con, logFile.getId(), "FAIL", "load to wh data fail");
            //12.3cập nhật trạng thái xử li file_log là đang xu ly = false
            phonePriceDao.updateIsProcessing(con, logFile.getId(), false);
            // 12.4 Gửi Mail đến Author
            String mail = logFile.getConfigFile().getEmail();
            DateTimeFormatter dt = DateTimeFormatter.ofPattern("hh:mm:ss dd/MM/yyyy");
            LocalDateTime nowTime = LocalDateTime.now();
            String timeNow = nowTime.format(dt);
            String subject = "Error Date: " + timeNow;
            String message = "Error with message: "+e.getMessage();
            SendMail.sendMail(mail, subject, message);

        }
    }
    public void extractToStaging(Connection con, LogFile logFile) throws SQLException {
        PhonePriceDao phonePriceDao = new PhonePriceDao();

        //6.Cập nhật  trạng thái  xử lí log_file là đang xử lý (isProcessing=true)
        phonePriceDao.updateIsProcessing(con, logFile.getId(), true);

        // 7. Cập nhật status log_file là EXTRACTING
        phonePriceDao.updateStatus(con, logFile.getId(), "EXTRACTING");

        // 8. Ghi Log đang tiến hành trich xuat du lieu
        phonePriceDao.insertLog(con, logFile.getId(), "EXTRACTING", "Start EXTRACTING to Staging ");

        // 9. Truncate table staging trong database staging
        truncateTable(con, logFile);

        String sqlLoadData = "LOAD DATA INFILE ? \n" +
                "INTO TABLE staging.phone_price_dailys_origin\n" +
                "FIELDS TERMINATED BY ',' \n" +
                "ENCLOSED BY '\"'\n" +
                "LINES TERMINATED BY '\\n'\n" +
                "IGNORE 1 ROWS\n" +
                "(NAME, price, processor, capacity, ram, screen_size, trademark, SOURCE, create_at);";
        //11. Load Du Lieu Vao Staging
        try{
            PreparedStatement psLoadData = con.prepareStatement(sqlLoadData);
            psLoadData.setString(1, logFile.getDetailFilePath());
            psLoadData.execute();

            System.out.println(" Da Vao Day");

            //12.
            phonePriceDao.updateStatus(con, logFile.getId(), "EXTRACTED");

            // 13. Ghi Log đang trich xuat du lieu thanh cong
            phonePriceDao.insertLog(con, logFile.getId(), "EXTRACTED", "EXTRACT to Staging Success");

            //14.Cập nhật  trạng thái  xử lí log_file là đang xử lý (isProcessing=false)
            phonePriceDao.updateIsProcessing(con, logFile.getId(), false);


        }catch (SQLException e){
            // 11.1 Xét trạng thái log_file: FAILED
            phonePriceDao.updateStatus(con, logFile.getId(), "FAILED");
            // 11.2 Ghi Log: CRAWL dữ liệu thất bại
            phonePriceDao.insertLog(con, logFile.getId(), "FAILED", "EXTRACT Data FAILED WITH MESSAGE "+ e.getMessage());
            // 11.3.Cập nhật  trạng thái  xử lí log_file là đang xử lý (isProcessing=false)
            phonePriceDao.updateIsProcessing(con, logFile.getId(), false);

            String mail = logFile.getConfigFile().getEmail();
            DateTimeFormatter dt = DateTimeFormatter.ofPattern("hh:mm:ss dd/MM/yyyy");
            LocalDateTime nowTime = LocalDateTime.now();
            String timeNow = nowTime.format(dt);
            String subject = "Error Date: " + timeNow;
            String message = "Error with message: "+e.getMessage();
            // 11.4 Gửi Mail đến Author
            SendMail.sendMail(mail, subject, message);
        }
    }

    public static void truncateTable(Connection connection, LogFile logFile) throws SQLException {
        PhonePriceDao dao = new PhonePriceDao();
        try (CallableStatement callableStatement = connection.prepareCall("{CALL truncate_staging_table()}")) {
            callableStatement.execute();
            dao.insertLog(connection, logFile.getId(), "EXTRACTING", "Truncate success");
        } catch (SQLException e) {
            e.printStackTrace();
            dao.insertLog(connection, logFile.getId(), "ERROR", "Error with message: "+e.getMessage());
            String mail = logFile.getConfigFile().getEmail();
            DateTimeFormatter dt = DateTimeFormatter.ofPattern("hh:mm:ss dd/MM/yyyy");
            LocalDateTime nowTime = LocalDateTime.now();
            String timeNow = nowTime.format(dt);
            String subject = "Error Date: " + timeNow;
            String message = "Error with message: "+e.getMessage();
            SendMail.sendMail(mail, subject, message);
        }
    }
    public void loadToAggregate(Connection con, LogFile logFile) throws SQLException {
        PhonePriceDao phonePriceDao = new PhonePriceDao();

        // 1. Đánh dấu trạng thái xử lý của log_file là đang xử lý (isProcessing=true)
        phonePriceDao.updateIsProcessing(con, logFile.getId(), true);

        // 2. Cập nhật trạng thái log_file là "AGGREGATING" để bắt đầu quá trình tổng hợp dữ liệu
        phonePriceDao.updateStatus(con, logFile.getId(), "AGGREGATING");

        // 3. Ghi log thông báo rằng quá trình tổng hợp dữ liệu đang bắt đầu
        phonePriceDao.insertLog(con, logFile.getId(), "AGGREGATING", "Start aggregating data from warehouse to aggregate table");

        // 4. Truncate bảng aggregate để làm sạch dữ liệu trước khi tổng hợp
//	    truncateAggregateTable(con, logFile);

        // 5. Thực thi stored procedure `load_to_aggregate` để tổng hợp dữ liệu từ `warehouse.phone_price_fact` vào `warehouse.phone_price_aggregate`
        try (CallableStatement callableStatement = con.prepareCall("{CALL load_to_aggregate()}")) {
            callableStatement.execute();

            // 6. Sau khi thực thi thành công, cập nhật trạng thái log_file là "AGGREGATED"
            phonePriceDao.updateStatus(con, logFile.getId(), "AGGREGATED");

            // 7. Ghi log thông báo rằng quá trình tổng hợp dữ liệu thành công
            phonePriceDao.insertLog(con, logFile.getId(), "AGGREGATED", "Data aggregated successfully from warehouse to aggregate table");

            // 8. Đánh dấu trạng thái xử lý của log_file là đã hoàn tất (isProcessing=false)
            phonePriceDao.updateIsProcessing(con, logFile.getId(), false);

            phonePriceDao.setFlagIsZero(con, logFile.getId());

        } catch (SQLException e) {
            // 9. Nếu có lỗi, cập nhật trạng thái log_file thành "FAILED"
            phonePriceDao.updateStatus(con, logFile.getId(), "FAILED");

            // 10. Ghi log thông báo lỗi kèm theo thông tin chi tiết
            phonePriceDao.insertLog(con, logFile.getId(), "FAILED", "Aggregation FAILED with message: " + e.getMessage());

            // 11. Đánh dấu trạng thái xử lý của log_file là đã hoàn tất (isProcessing=false)
            phonePriceDao.updateIsProcessing(con, logFile.getId(), false);

            // 12. Gửi email thông báo lỗi đến tác giả của log_file
            String mail = logFile.getConfigFile().getEmail();
            String subject = "Error: Aggregation Failed";
            String message = "An error occurred while aggregating data.\n\nDetails:\n" + e.getMessage();
            SendMail.sendMail(mail, subject, message);

            // 13. In thông báo lỗi ra màn hình (cho mục đích debug)
            System.out.println("Aggregation failed: " + e.getMessage());
        }
    }
//	  public static void truncateAggregateTable(Connection connection, LogFile logFile) {
//	        PhonePriceDao dao = new PhonePriceDao(); // DAO to log status and errors
//	        try (CallableStatement callableStatement = connection.prepareCall("{CALL truncate_aggregate_table()}")) {
//	            callableStatement.execute(); // Call the stored procedure
//	            // Log success in the log files
//	            dao.insertLog(connection, logFile.getId(), "AGGREGATING", "Aggregate table truncated successfully");
//	            System.out.println("Aggregate table truncated successfully.");
//	        } catch (SQLException e) {
//	            // Handle exception and log the error
//	            e.printStackTrace();
//	            dao.insertLog(connection, logFile.getId(), "ERROR", "Error truncating aggregate table: " + e.getMessage());
//
//	            // Send email notification in case of error
//	            String mail = logFile.getConfigFile().getEmail();
//	            DateTimeFormatter dt = DateTimeFormatter.ofPattern("hh:mm:ss dd/MM/yyyy");
//	            LocalDateTime nowTime = LocalDateTime.now();
//	            String timeNow = nowTime.format(dt);
//	            String subject = "Error Date: " + timeNow;
//	            String message = "Error with message: "+e.getMessage();
//	            SendMail.sendMail(mail, subject, message);
//
//	            System.out.println("Error truncating aggregate table: " + e.getMessage());
//	        }
//	    }

public void loadToAggregate(Connection con, LogFile logFile) throws SQLException {
	    PhonePriceDao phonePriceDao = new PhonePriceDao();

	    // 1. Đánh dấu trạng thái xử lý của log_file là đang xử lý (isProcessing=true)
	    phonePriceDao.updateIsProcessing(con, logFile.getId(), true);

	    // 2. Cập nhật trạng thái log_file là "AGGREGATING" để bắt đầu quá trình tổng hợp dữ liệu
	    phonePriceDao.updateStatus(con, logFile.getId(), "AGGREGATING");

	    // 3. Ghi log thông báo rằng quá trình tổng hợp dữ liệu đang bắt đầu
	    phonePriceDao.insertLog(con, logFile.getId(), "AGGREGATING", "Start aggregating data from warehouse to aggregate table");

	    // 4. Truncate bảng aggregate để làm sạch dữ liệu trước khi tổng hợp
//	    truncateAggregateTable(con, logFile);

	    // 5. Thực thi stored procedure `load_to_aggregate` để tổng hợp dữ liệu từ `warehouse.phone_price_fact` vào `warehouse.phone_price_aggregate`
	    try (CallableStatement callableStatement = con.prepareCall("{CALL load_to_aggregate()}")) {
	        callableStatement.execute();

	        // 6. Sau khi thực thi thành công, cập nhật trạng thái log_file là "AGGREGATED"
	        phonePriceDao.updateStatus(con, logFile.getId(), "AGGREGATED");

	        // 7. Ghi log thông báo rằng quá trình tổng hợp dữ liệu thành công
	        phonePriceDao.insertLog(con, logFile.getId(), "AGGREGATED", "Data aggregated successfully from warehouse to aggregate table");

	        // 8. Đánh dấu trạng thái xử lý của log_file là đã hoàn tất (isProcessing=false)
	        phonePriceDao.updateIsProcessing(con, logFile.getId(), false);
	        
	        phonePriceDao.setFlagIsZero(con, logFile.getId());

	    } catch (SQLException e) {
	        // 9. Nếu có lỗi, cập nhật trạng thái log_file thành "FAILED"
	        phonePriceDao.updateStatus(con, logFile.getId(), "FAILED");

	        // 10. Ghi log thông báo lỗi kèm theo thông tin chi tiết
	        phonePriceDao.insertLog(con, logFile.getId(), "FAILED", "Aggregation FAILED with message: " + e.getMessage());

	        // 11. Đánh dấu trạng thái xử lý của log_file là đã hoàn tất (isProcessing=false)
	        phonePriceDao.updateIsProcessing(con, logFile.getId(), false);

	        // 12. Gửi email thông báo lỗi đến tác giả của log_file
	        String mail = logFile.getConfigFile().getEmail();
	        String subject = "Error: Aggregation Failed";
	        String message = "An error occurred while aggregating data.\n\nDetails:\n" + e.getMessage();
	        SendMail.sendMail(mail, subject, message);

	        // 13. In thông báo lỗi ra màn hình (cho mục đích debug)
	        System.out.println("Aggregation failed: " + e.getMessage());
	    }
	}
//	  public static void truncateAggregateTable(Connection connection, LogFile logFile) {
//	        PhonePriceDao dao = new PhonePriceDao(); // DAO to log status and errors
//	        try (CallableStatement callableStatement = connection.prepareCall("{CALL truncate_aggregate_table()}")) {
//	            callableStatement.execute(); // Call the stored procedure
//	            // Log success in the log files
//	            dao.insertLog(connection, logFile.getId(), "AGGREGATING", "Aggregate table truncated successfully");
//	            System.out.println("Aggregate table truncated successfully.");
//	        } catch (SQLException e) {
//	            // Handle exception and log the error
//	            e.printStackTrace();
//	            dao.insertLog(connection, logFile.getId(), "ERROR", "Error truncating aggregate table: " + e.getMessage());
//	            
//	            // Send email notification in case of error
//	            String mail = logFile.getConfigFile().getEmail();
//	            DateTimeFormatter dt = DateTimeFormatter.ofPattern("hh:mm:ss dd/MM/yyyy");
//	            LocalDateTime nowTime = LocalDateTime.now();
//	            String timeNow = nowTime.format(dt);
//	            String subject = "Error Date: " + timeNow;
//	            String message = "Error with message: "+e.getMessage();
//	            SendMail.sendMail(mail, subject, message);
//	            
//	            System.out.println("Error truncating aggregate table: " + e.getMessage());
//	        }
//	    }

}
