package dao;

import database.DBConnection;
import entity.LogFile;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PhonePriceDao {


    public static void insertFileLog(Connection connection, int idConfig, String date, String status) {
        String procedureCall = "{CALL InsertLogFile(?,?,?)}"; // Cập nhật câu gọi Procedure với OUT parameter
        try (CallableStatement callableStatement = connection.prepareCall(procedureCall)) {
            // Đặt tham số đầu vào (IN)
            callableStatement.setInt(1, idConfig);
            callableStatement.setString(2, date);
            callableStatement.setString(3, status);
            callableStatement.execute();

        } catch (SQLException e) {
            throw new RuntimeException("Lỗi khi gọi Procedure InsertLogFile: " + e.getMessage(), e);
        }
    }

    public static void updateStatus(Connection connection, int id, String status) {
        try (CallableStatement callableStatement = connection.prepareCall("{CALL UpdateStatus(?,?)}")) {
            callableStatement.setInt(1, id);
            callableStatement.setString(2, status);
            callableStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateDetailFilePath(Connection connection, int id, String detailFilePath) {
        try (CallableStatement callableStatement = connection.prepareCall("{CALL UpdatePathFileDetail(?,?)}")) {
            callableStatement.setInt(1, id);
            callableStatement.setString(2, detailFilePath);
            callableStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setFlagIsZero(Connection connection, int id) {
        try (CallableStatement callableStatement = connection.prepareCall("{CALL SetFlagIsZero(?)}")) {
            callableStatement.setInt(1, id);
            callableStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void insertLog(Connection connection, int idConfig, String status, String description) {
        try (CallableStatement callableStatement = connection.prepareCall("{Call InsertLog(?,?,?)}")) {
            callableStatement.setInt(1, idConfig);
            callableStatement.setString(2, status);
            callableStatement.setString(3, description);
            callableStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getLogs(Connection connection, int idConfig) {
        List<String> logs = new ArrayList<>();
        //Câu select lấy list config muốn run
        String query = "SELECT * FROM log WHERE id_config = ? ORDER BY created_at ASC";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, idConfig);
            ResultSet resultSet = statement.executeQuery();
            int i = 1;
            while (resultSet.next()) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(i++ + ". ");
                stringBuilder.append("ID Config: " + resultSet.getInt("id_config"));
                stringBuilder.append(". Status: " + resultSet.getString("status"));
                stringBuilder.append(". Description: " + resultSet.getString("description"));
                stringBuilder.append("Time: " + resultSet.getTimestamp("created_at").toString());
                logs.add(stringBuilder.toString());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return logs;
    }

    public static void updateIsProcessing(Connection connection, int id, boolean isProcessing) {
        try (CallableStatement callableStatement = connection.prepareCall("{CALL UpdateIsProcessing(?,?)}")) {
            callableStatement.setInt(1, id);
            if (isProcessing) {
                callableStatement.setInt(2, 1);
            } else {
                callableStatement.setInt(2, 0);
            }
            callableStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getProcessingCount(Connection connection) {
        int count = 0;
        String query = "SELECT * FROM log_files WHERE is_processing = true";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                count++;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return count;
    }

    public static void main(String[] args) {
        DBConnection dbConnection = new DBConnection();
        Connection connection = dbConnection.getConnection();
        PhonePriceDao.insertFileLog(connection, 1, "2024-11-28", "PENDING");
    }
}
