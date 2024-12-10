package entity;

public class ConfigTable {
    private int id;
    private String nameTbStagingOrigin;
    private String nameTbStagingTransformed;
    private String nameTbWarehousePhoneDim;
    private String nameTbWarehouseDateDim;
    private String nameTbWarehousePhoneFact;
    private String nameTbWarehouseAggregate;
    private String nameTbMart;

    public ConfigTable() {
    }

    public ConfigTable(int id, String nameTbStagingOrigin, String nameTbStagingTransformed,
                       String nameTbWarehousePhoneDim, String nameTbWarehouseDateDim,
                       String nameTbWarehousePhoneFact, String nameTbWarehouseAggregate,
                       String nameTbMart) {
        this.id = id;
        this.nameTbStagingOrigin = nameTbStagingOrigin;
        this.nameTbStagingTransformed = nameTbStagingTransformed;
        this.nameTbWarehousePhoneDim = nameTbWarehousePhoneDim;
        this.nameTbWarehouseDateDim = nameTbWarehouseDateDim;
        this.nameTbWarehousePhoneFact = nameTbWarehousePhoneFact;
        this.nameTbWarehouseAggregate = nameTbWarehouseAggregate;
        this.nameTbMart = nameTbMart;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNameTbStagingOrigin() {
        return nameTbStagingOrigin;
    }

    public void setNameTbStagingOrigin(String nameTbStagingOrigin) {
        this.nameTbStagingOrigin = nameTbStagingOrigin;
    }

    public String getNameTbStagingTransformed() {
        return nameTbStagingTransformed;
    }

    public void setNameTbStagingTransformed(String nameTbStagingTransformed) {
        this.nameTbStagingTransformed = nameTbStagingTransformed;
    }

    public String getNameTbWarehousePhoneDim() {
        return nameTbWarehousePhoneDim;
    }

    public void setNameTbWarehousePhoneDim(String nameTbWarehousePhoneDim) {
        this.nameTbWarehousePhoneDim = nameTbWarehousePhoneDim;
    }

    public String getNameTbWarehouseDateDim() {
        return nameTbWarehouseDateDim;
    }

    public void setNameTbWarehouseDateDim(String nameTbWarehouseDateDim) {
        this.nameTbWarehouseDateDim = nameTbWarehouseDateDim;
    }

    public String getNameTbWarehousePhoneFact() {
        return nameTbWarehousePhoneFact;
    }

    public void setNameTbWarehousePhoneFact(String nameTbWarehousePhoneFact) {
        this.nameTbWarehousePhoneFact = nameTbWarehousePhoneFact;
    }

    public String getNameTbWarehouseAggregate() {
        return nameTbWarehouseAggregate;
    }

    public void setNameTbWarehouseAggregate(String nameTbWarehouseAggregate) {
        this.nameTbWarehouseAggregate = nameTbWarehouseAggregate;
    }

    public String getNameTbMart() {
        return nameTbMart;
    }

    public void setNameTbMart(String nameTbMart) {
        this.nameTbMart = nameTbMart;
    }

    @Override
    public String toString() {
        return "ConfigTable{" +
                "id=" + id +
                ", nameTbStagingOrigin='" + nameTbStagingOrigin + '\'' +
                ", nameTbStagingTransformed='" + nameTbStagingTransformed + '\'' +
                ", nameTbWarehousePhoneDim='" + nameTbWarehousePhoneDim + '\'' +
                ", nameTbWarehouseDateDim='" + nameTbWarehouseDateDim + '\'' +
                ", nameTbWarehousePhoneFact='" + nameTbWarehousePhoneFact + '\'' +
                ", nameTbWarehouseAggregate='" + nameTbWarehouseAggregate + '\'' +
                ", nameTbMart='" + nameTbMart + '\'' +
                '}';
    }
}
