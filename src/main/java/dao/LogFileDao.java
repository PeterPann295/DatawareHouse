package dao;

import entity.LogFile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class LogFileDao {


        public static LogFile getLogFile(Connection conn, int configFileId, Date date) {
            String query = "SELECT * FROM log_files WHERE id_config = ? AND DATE(created_at) = ?";
            LogFile logFile = null;

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, configFileId);
                pstmt.setDate(2, new java.sql.Date(date.getTime())); // Chuyển từ java.util.Date sang java.sql.Date

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        logFile = new LogFile();
                        logFile.setId(rs.getInt("id"));
                        logFile.setConfigFile(ConfigFileDao.getConfigFile(conn, rs.getInt("id_config")));
                        logFile.setFileName(rs.getString("file_name"));
                        logFile.setDetailFilePath(rs.getString("detail_file_path"));
                        logFile.setIsProcessing(rs.getBoolean("is_processing"));
                        logFile.setStatus(rs.getString("status"));
                        logFile.setCreateAt(rs.getTimestamp("created_at"));
                        logFile.setUpdateAt(rs.getTimestamp("updated_at"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return logFile;
        }

    public static LogFile getLogFileById(Connection conn, int id) {
        String query = "SELECT * FROM log_files WHERE id=?";
        LogFile logFile = null;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    logFile = new LogFile();
                    logFile.setId(rs.getInt("id"));
                    logFile.setConfigFile(ConfigFileDao.getConfigFile(conn, rs.getInt("id_config")));
                    logFile.setFileName(rs.getString("file_name"));
                    logFile.setDetailFilePath(rs.getString("detail_file_path"));
                    logFile.setIsProcessing(rs.getBoolean("is_processing"));
                    logFile.setStatus(rs.getString("status"));
                    logFile.setCreateAt(rs.getTimestamp("created_at"));
                    logFile.setUpdateAt(rs.getTimestamp("updated_at"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logFile;
    }
    }


