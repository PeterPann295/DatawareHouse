package stage;

import controller.Controller;
import dao.LogFileDao;
import dao.PhonePriceDao;
import database.DBConnection;
import entity.LogFile;
import util.ArgumentValidator;

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TranformData {
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
    //3.Kết nối DB Control
        try(
    Connection connection = db.getConnection()) {

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
            LogFile fileLog = LogFileDao.getLogFile(connection ,idConfigFile, date.toString());
            //5. Kiem Tra LogFile Da Duoc Tao Hay Chua
            if (fileLog == null) {
                //5.1 ghi file_log tranform thất bại
                dao.insertFileLog(connection, fileLog.getId(), "FAIL", "Tranform data fail");
            }
            // 6. Lấy status của file log
            String status = fileLog.getStatus();
            //7. Kiểm tra xem status có phải laf extracted không
            if(status.equalsIgnoreCase("extracted")){
                //8. cap nhat trang thai file log là transforming
                dao.updateStatus(connection, fileLog.getId(), "TRANSFORMING");
                //9. ghi log dang transform
                dao.insertLog(connection, fileLog.getId(), "", "transforming");
                //10. Tiến hành tranform
                controller.transfromData(connection, fileLog);
            } else {
                //7.1 ghi log thaast bại nếu không phải extracted
                dao.insertFileLog(connection, fileLog.getId(), "FAIL", "Tranform data fail");
            }
        db.closeConnection();
        }catch (Exception e) {
            throw new RuntimeException(e);

        }

}
}
