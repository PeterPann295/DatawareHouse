package controller;

import com.opencsv.CSVWriter;
import dao.PhonePriceDao;
import entity.LogFile;
import util.FakePhonePriceDaily;
import util.SendMail;

import java.io.FileWriter;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Controller {
    public void getData(Connection con, LogFile logFile){
        PhonePriceDao phonePriceDao = new PhonePriceDao();

        phonePriceDao.updateIsProcessing(con, logFile.getId(), true);

        phonePriceDao.updateStatus(con, logFile.getId(), "CRAWLING");

        phonePriceDao.insertLog(con, logFile.getId(), "CRAWLING", "Start craw data");

        String detailFilePath = logFile.getDetailFilePath() +".csv";

        try {
            // Tạo writer để ghi file CSV
            CSVWriter writer = new CSVWriter(new FileWriter(detailFilePath));

            String[] header = {
                    "NAME", "price", "processor", "capacity", "ram", "screen_size", "trademark", "SOURCE", "create_at"
            };
            writer.writeNext(header);

            // Lấy dữ liệu giả lập và ghi vào file CSV
            List<String[]> fakeData = FakePhonePriceDaily.generateFakeData(10); // 10 bản ghi giả lập
            writer.writeAll(fakeData);

            writer.close();
            System.out.println("File CSV đã được tạo tại: " + detailFilePath);

            System.out.println("CRAWLED success");

            phonePriceDao.updateStatus(con, logFile.getId(), "CRAWLED");
            phonePriceDao.insertLog(con, logFile.getId(), "CRAWLED", "End crawl, data to "+ detailFilePath);

            phonePriceDao.updateIsProcessing(con, logFile.getId(), false);
        } catch (Exception e) {
            phonePriceDao.updateStatus(con, logFile.getId(), "FAILED");
            phonePriceDao.insertLog(con, logFile.getId(), "FAILED", "FAILED WITH MESSAGE "+ e.getMessage());

            phonePriceDao.updateIsProcessing(con, logFile.getId(), false);

            String mail = logFile.getConfigFile().getEmail();
            DateTimeFormatter dt = DateTimeFormatter.ofPattern("hh:mm:ss dd/MM/yyyy");
            LocalDateTime nowTime = LocalDateTime.now();
            String timeNow = nowTime.format(dt);
            String subject = "Error Date: " + timeNow;
            String message = "Error with message: "+e.getMessage();
            SendMail.sendMail(mail, subject, message);
        }


    }
}
