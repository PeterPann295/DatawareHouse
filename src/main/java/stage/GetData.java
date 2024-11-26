package stage;

import dao.PhonePriceDao;
import database.DBConnection;
import entity.FileLog;

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GetData {
    public static void main(String[] args) throws ParseException {
        if (args.length < 2) {
            System.out.println("Vui lòng truyền id_config và date dưới dạng tham số!");
            System.out.println("Cú pháp: java -jar GetData.jar <id_config_file> <date>");
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

        try(Connection connection = db.getConnection()) {

            int countProcessing = dao.getProcessingCount(connection);
            // Kiem Tra Co Process Dang Duoc Thuc Thi
            if (countProcessing > 0) {
                int maxWait = 0;
                // Cho Doi 3 Phut
                while (dao.getProcessingCount(connection) != 0 && maxWait <= 3) {
                    System.out.println("Wait...");
                    maxWait++;
                    Thread.sleep(60000); //60s
                }
            }

            FileLog fileLog = dao.getFileLog(idConfigFile, date);
            if (fileLog == null) {
                
            }





        }catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
