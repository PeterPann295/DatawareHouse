package util;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ArgumentValidator {

    public static boolean validateArgs(String[] args) {
        // Kiểm tra số lượng tham số
        if (args.length < 2) {
            System.out.println("Vui lòng truyền id_config và date dưới dạng tham số!");
            System.out.println("Cú pháp: java -jar GetData.jar <id_config_file> <date>");
            return false;
        }

        String idConfigFileString = args[0];
        String dateString = args[1];
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);

        try {
            // Kiểm tra id_config_file (phải là số nguyên dương)
            int idConfigFile = Integer.parseInt(idConfigFileString);
            if (idConfigFile <= 0) {
                throw new NumberFormatException("id_config_file phải là số nguyên dương.");
            }

            // Kiểm tra ngày (phải đúng định dạng yyyy-MM-dd)
            Date date = dateFormat.parse(dateString);

            // Nếu cả hai đều hợp lệ
            System.out.println("id_config_file hợp lệ: " + idConfigFile);
            System.out.println("Ngày hợp lệ: " + date);
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Lỗi: id_config_file phải là một số nguyên dương.");
            System.out.println("Cú pháp: java -jar GetData.jar <id_config_file> <date>");
            return false;
        } catch (ParseException e) {
            System.out.println("Lỗi: ngày không đúng định dạng yyyy-MM-dd.");
            System.out.println("Cú pháp: java -jar GetData.jar <id_config_file> <date>");
            return false;
        }
    }
}

