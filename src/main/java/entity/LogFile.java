package entity;

import java.util.Date;

public class LogFile {

    private int id;
    private ConfigFile configFile;
    private String fileName;
    private String detailFilePath;
    private boolean isProcessing;
    private String status;
    private Date createAt;
    private Date updateAt;

    public LogFile(int id, ConfigFile configFile, String fileName, String detailFilePath, boolean isProcessing, String status, Date createAt, Date updateAt) {
        this.id = id;
        this.configFile = configFile;
        this.fileName = fileName;
        this.detailFilePath = detailFilePath;
        this.isProcessing = isProcessing;
        this.status = status;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }
    public LogFile(){

    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ConfigFile getConfigFile() {
        return configFile;
    }

    public void setConfigFile(ConfigFile configFile) {
        this.configFile = configFile;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDetailFilePath() {
        return detailFilePath;
    }

    public void setDetailFilePath(String detailFilePath) {
        this.detailFilePath = detailFilePath;
    }

    public boolean isProcessing() {
        return isProcessing;
    }
    public void setIsProcessing(boolean isProcessing) {
        this.isProcessing = isProcessing;
    }

    public void setProcessing(boolean processing) {
        isProcessing = processing;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public Date getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Date updateAt) {
        this.updateAt = updateAt;
    }

    @Override
    public String toString() {
        return "LogFile{" +
                "id=" + id +
                ", configFile=" + configFile.getId() +
                ", fileName='" + fileName + '\'' +
                ", detailFilePath='" + detailFilePath + '\'' +
                ", isProcessing=" + isProcessing +
                ", status='" + status + '\'' +
                ", createAt=" + createAt +
                ", updateAt=" + updateAt +
                '}';
    }
}
