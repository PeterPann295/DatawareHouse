package stage;

import controller.Controller;
import dao.LogFileDao;
import dao.PhonePriceDao;
import database.DBConnection;
import entity.LogFile;
import util.ArgumentValidator;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoadToWarehouse {
    public static void main(String[] args) throws ParseException {
        int idConfigFile = Integer.parseInt(args[0]);
        String dateString = args[1];
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = dateFormat.parse(dateString);

        System.out.println("id_config: " + idConfigFile);
        System.out.println("date: " + date);

        DBConnection db = new DBConnection();
        PhonePriceDao dao = new PhonePriceDao();
        Controller controller = new Controller();
        //2.Kết nối DB Control
        try (Connection connection = db.getConnection()) {
            // 3. Kiểm tra xem kết nối có hợp lệ không
            if (connection == null || !connection.isValid(5)) { // 5 giây là timeout
                //3.1 Báo lỗi kết nối không thành công
                throw new SQLException("DB connect thất bại");
            }

            //4. Kiem Tra Co Process Dang Duoc Thuc Thi
            int countProcessing = dao.getProcessingCount(connection);
            if (countProcessing > 0) {
                int maxWait = 0;
                //4.1 Cho Doi 3 Phut
                while (dao.getProcessingCount(connection) != 0 && maxWait <= 3) {
                    System.out.println("Wait...");
                    maxWait++;
                    Thread.sleep(60000); //60s
                }
            }


            LogFile fileLog = LogFileDao.getLogFile(connection, idConfigFile, dateString);
            //5. Kiem Tra LogFile Da Duoc Tao Hay Chua
            if (fileLog == null) {
                //5.1 ghi log load data thất bại
                dao.insertLog(connection, fileLog.getId(), "FAIL", "load data to wh thất bại");
                throw new RuntimeException("load data thất bại: fileLog is null");
            }
            // 6. Lấy status của file log
            String status = fileLog.getStatus();
            //7. Kiểm tra xem status có phải laf transformed không
            if(status.equalsIgnoreCase("transformed")){
                //8. cap nhat trang thai xu ly file log la dang xu ly
                dao.updateIsProcessing(connection, fileLog.getId(), true);
                //9. cap nhat trang thai của file log
                dao.updateStatus(connection, fileLog.getId(), "WH_LOADING");
                //10. Ghi log bat dau load to wh
                dao.insertLog(connection, fileLog.getId(), "", "Start load to warehouse");
                //11. Tiến hành load data
                controller.loadToWarehouse(connection, fileLog);
            } else {
                //7.1 ghi log thaast bại nếu không phải extracted
                dao.insertLog(connection, fileLog.getId(), "FAIL", "load data to wh thất bại");
            }
            db.closeConnection();
        }catch (Exception e) {
            throw new RuntimeException(e);

        }

    }
}
