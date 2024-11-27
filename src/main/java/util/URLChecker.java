package util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class URLChecker {
    public static boolean isURLActive(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD"); // Sử dụng HEAD thay vì GET để tiết kiệm tài nguyên
            connection.setConnectTimeout(5000); // Thời gian chờ kết nối (ms)
            connection.setReadTimeout(5000);    // Thời gian chờ đọc dữ liệu (ms)
            connection.connect();

            int responseCode = connection.getResponseCode();
            return (responseCode >= 200 && responseCode < 400); // URL hoạt động nếu mã phản hồi là 2xx hoặc 3xx
        } catch (IOException e) {
            return false; // URL không hoạt động
        }
    }

    public static void main(String[] args) {
        String testURL = "https://fptshop.com.vn";
        if (isURLActive(testURL)) {
            System.out.println("URL hoạt động!");
        } else {
            System.out.println("URL không hoạt động!");
        }
    }
}
