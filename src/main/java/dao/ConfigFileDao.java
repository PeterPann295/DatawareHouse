package dao;

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
                    configFile.setSource(rs.getString("source"));
                    configFile.setId(id);
                    configFile.setDirectoryFile(rs.getString("directory_file"));
                    configFile.setAuthor(rs.getString("author"));
                    configFile.setEmail(rs.getString("email"));
                    configFile.setCreateAt(rs.getTimestamp("create_at"));
                    configFile.setUpdateAt(rs.getTimestamp("update_at"));
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return configFile;
    }
}
