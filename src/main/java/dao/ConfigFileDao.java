package dao;

import database.DBConnection;
import entity.ConfigFile;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfigFileDao {
    public static String getSource(Connection con, int id) {
        String query = "SELECT source FROM config_files WHERE id = ?";
        String source = null;

        try (PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) { // Nếu có kết quả
                    source = rs.getString("source");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return source;
    }
    public static ConfigFile getConfigFile(Connection con, int id) {
        String query = "SELECT * FROM config_files WHERE id = ?";

        ConfigFile configFile = null;
        try(PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    configFile = new ConfigFile();
                    configFile.setSource(rs.getString("source"));
                    configFile.setId(id);
                    configFile.setDirectoryFile(rs.getString("directory_file"));
                    configFile.setAuthor(rs.getString("author"));
                    configFile.setConfigTable(ConfigTableDao.getConfigTable(con, rs.getInt("id_config_table")));
                    configFile.setEmail(rs.getString("email"));
                    configFile.setCreateAt(rs.getTimestamp("created_at"));
                    configFile.setUpdateAt(rs.getTimestamp("updated_at"));
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return configFile;
    }

    public static void main(String[] args) {
        DBConnection dbConnection = new DBConnection();
        Connection con = dbConnection.getConnection();
        System.out.println(getConfigFile(con, 1));
    }
}
