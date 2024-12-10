package dao;

import entity.ConfigFile;
import entity.ConfigTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfigTableDao {
    public static ConfigTable getConfigTable(Connection con, int id) {
        String query = "SELECT * FROM config_tables WHERE id = ?";
        ConfigTable configTable = null;
        try(PreparedStatement pstmt = con.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    configTable = new ConfigTable();
                    configTable.setId(rs.getInt("id"));
                    configTable.setNameTbStagingOrigin(rs.getString("name_tb_staging_origin"));
                    configTable.setNameTbStagingTransformed(rs.getString("name_tb_staging_transformed"));
                    configTable.setNameTbWarehouseDateDim(rs.getString("name_tb_warehouse_date_dim"));
                    configTable.setNameTbWarehousePhoneDim(rs.getString("name_tb_warehouse_phone_dim"));
                    configTable.setNameTbWarehouseAggregate(rs.getString("name_tb_warehouse_aggregate"));
                    configTable.setNameTbMart(rs.getString("name_tb_mart"));
                }
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return configTable;
    }
}
