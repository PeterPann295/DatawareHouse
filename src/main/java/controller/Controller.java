package controller;

import com.opencsv.CSVWriter;
import dao.PhonePriceDao;
import entity.LogFile;
import util.FakePhonePriceDaily;
import util.SendMail;
import util.URLChecker;

import java.io.FileWriter;
import java.sql.Connection;
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
        if(!URLChecker.isURLActive(src)){
            // 11.1 Ghi log kết nối đến src Web thất bại
            phonePriceDao.insertLog(con, logFile.getId(), "FAILED", "Connect to source web failed");
            // 11.2 Xét trạng thái File_log: FAILED
            phonePriceDao.updateStatus(con, logFile.getId(), "FAILED");
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
}
