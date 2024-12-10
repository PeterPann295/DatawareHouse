package stage;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import dao.PhonePriceDao;
import controller.Controller;
import dao.LogFileDao;
import database.DBConnection;
import entity.LogFile;
import util.ArgumentValidator;

public class LoadToDataMart {
    public static void main(String[] args) throws ParseException, SQLException, InterruptedException {
        // Bước 1: Kiểm tra Input
        if (!ArgumentValidator.validateArgs(args)) {
            // Bước 2: Yêu cầu người dùng chạy lại Jar nếu tham số không hợp lệ
            System.out.println("Tham số không hợp lệ vui lòng truyền lại");
            return;
        }

        // Đọc giá trị từ tham số đầu vào
        int idConfigFile = Integer.parseInt(args[0]);
        String dateString = args[1];
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = dateFormat.parse(dateString);

        System.out.println("id_config: " + idConfigFile);
        System.out.println("date: " + date);

        //Bước 2 Kết nối DB và chuẩn bị các DAO
        DBConnection db = new DBConnection();
        PhonePriceDao dao = new PhonePriceDao();
        Controller controller = new Controller();

        try (Connection connection = db.getConnection()) {
            // Bước 3: Kiểm tra kết nối DB
            if (connection == null || !connection.isValid(5)) {
                //  Nếu kết nối không được thì ghi log kết nối DB thất bại
                System.out.println("Kết nối DB thất bại");
                dao.insertLog(connection, idConfigFile, "FAILED", "Kết nối DB thất bại");
                return;
            }

            // Bước 4: Kiểm tra process có đang thực thi không
            int countProcessing = dao.getProcessingCount(connection);
            if (countProcessing > 0) {
                // Bước 4.1: Nếu có process đang thực thi, chờ 3 phút để process khác thực thi
                int maxWait = 0;
                while (dao.getProcessingCount(connection) > 0 && maxWait < 3) {
                    System.out.println("Chờ đợi quá trình khác thực thi...");
                    maxWait++;
                    Thread.sleep(60000); // Chờ 60 giây
                }
            }

            // Bước 5: Kiểm tra file log có tồn tại không
            LogFile fileLog = LogFileDao.getLogFile(connection, idConfigFile, dateString);
            if (fileLog == null) {
                // Bước 6.1: Nếu file log không tồn tại thì ghi log thất bại
                System.out.println("File log không tồn tại");
                dao.insertLog(connection, idConfigFile, "FAILED", "File log không tồn tại");
                return;
            }
            // Bước 6: Lấy trạng thái của file log
            String status = fileLog.getStatus();

            // Bước 6.1: Kiểm tra trạng thái file log là AGGREGATED hay không
            if (!"AGGREGATED".equals(status)) {
                // Bước 6.2: Nếu không phải AGGREGATED, ghi log thất bại và kết thúc
                System.out.println("Trạng thái file log không phải AGGREGATED");
                dao.insertLog(connection, idConfigFile, "FAILED", "Trạng thái file log không phải AGGREGATED");
                return;
            }

            // Bước 7: Cập nhật trạng thái file log là đang xử lý
            dao.updateFileLogStatus(connection, fileLog.getId(), "isProcessing", true);

            // Bước 8: Cập nhật status file_log là MLOADING
            dao.updateStatus(connection, fileLog.getId() , "MLOADING");

            // Bước 9: Ghi log bắt đầu load vào DataMart
            dao.insertLog(connection, fileLog.getId(), "STARTED", "Bắt đầu load dữ liệu vào DataMart");

            try {
                // Bước 10: Tiến hành load dữ liệu vào DataMart
                controller.loadToDataMart(connection, fileLog);

                // Bước 11: Nếu thành công, cập nhật trạng thái file log là MLOADED
                dao.updateStatus(connection, fileLog.getId(),  "MLOADED");

                // Bước 12: Ghi log đã load dữ liệu thành công
                dao.insertLog(connection, fileLog.getId(), "COMPLETED", "Dữ liệu đã được load thành công vào DataMart");

            } catch (Exception e) {
                // Bước 10.1: Nếu quá trình load thất bại, cập nhật trạng thái file log là FAILED
                dao.updateStatus(connection, fileLog.getId(), "FAILED");

                // Bước 10.2: Ghi log load thất bại
                dao.insertLog(connection, fileLog.getId(), "FAILED", "Quá trình load dữ liệu thất bại: " + e.getMessage());

                // Bước 10.3: Cập nhật trạng thái file log isProcessing = false
                dao.updateFileLogStatus(connection, fileLog.getId(), "isProcessing", false);

                // Bước 10.4: Gửi email cho tác giả
                String authorEmail = dao.getAuthorEmail(connection, fileLog.getId());
                if (authorEmail != null) {
                    sendEmail(authorEmail, "Dữ liệu load thất bại", "Quá trình load dữ liệu vào DataMart đã thất bại.");
                }

                return;
            }

            // Bước 13: Cập nhật trạng thái file log isProcessing = false sau khi hoàn tất
            dao.updateFileLogStatus(connection, fileLog.getId(), "isProcessing", false);

        } catch (Exception e) {
            // Xử lý lỗi và ghi log thất bại
            e.printStackTrace();
            try (Connection connection = db.getConnection()) {
                LogFile fileLog = LogFileDao.getLogFile(connection, idConfigFile, dateString);
                if (fileLog != null) {
                    dao.insertLog(connection, fileLog.getId(), "FAILED", "Quá trình load dữ liệu thất bại: " + e.getMessage());
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            throw new RuntimeException("Có lỗi xảy ra trong quá trình load dữ liệu", e);
        }
    }

    // Hàm gửi email thông báo cho tác giả
    private static void sendEmail(String to, String subject, String body) {
        // Giả sử bạn có một hàm gửi email sử dụng JavaMail API hoặc một thư viện gửi email khác
        // Đây chỉ là ví dụ, bạn cần triển khai cụ thể hàm gửi email
        System.out.println("Gửi email cho tác giả: " + to);
        // Code gửi email ở đây...
    }
}
