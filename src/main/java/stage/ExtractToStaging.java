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

public class ExtractToStaging {
    public static void main(String[] args) throws ParseException, SQLException, InterruptedException {
        //1. Kiem tra Input
        if (!ArgumentValidator.validateArgs(args)) {
            // 2.1 Yeu Cau Nguoi Dung Chay Lai Jar
            System.out.println("Tham số không hợp lệ vui lòng truyền lại");
            return;
        }
        int idConfigFile = Integer.parseInt(args[0]);
        String dateString = args[1];
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = dateFormat.parse(dateString);

        System.out.println("id_config: " + idConfigFile);
        System.out.println("date: " + date);

        DBConnection db = new DBConnection();
        PhonePriceDao dao = new PhonePriceDao();
        Controller controller = new Controller();
        //3.Kết nối DB Control
        try (Connection connection = db.getConnection()) {

            int countProcessing = dao.getProcessingCount(connection);
            //4. Kiem Tra Co Process Dang Duoc Thuc Thi
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
                // 5.1 Ghi log Extract dữ liệu file_log thất bại
                dao.insertLog(connection, fileLog.getId(), "FAILED", "EXTRACT data failed");
            }
            // 6. Kiểm tra status log_file phải CRAWLED?
            if (fileLog.getStatus().equalsIgnoreCase("CRAWLED")) {
                controller.extractToStaging(connection, fileLog);
            } else {
                // 5.1 Ghi log Extract dữ liệu file_log thất bại
                dao.insertLog(connection, fileLog.getId(), "FAILED", "EXTRACT data failed");
            }
        }
    }
}
