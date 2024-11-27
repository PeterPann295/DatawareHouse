package entity;

import java.util.Date;

public class ConfigFile {
    private int id;
    private String author;
    private String email;
    private String source;
    private String directoryFile;
    private Date createAt;
    private Date updateAt;

    public ConfigFile(int id, String author, String email, String source, String directoryFile, Date createAt, Date updateAt) {
        this.id = id;
        this.author = author;
        this.email = email;
        this.source = source;
        this.directoryFile = directoryFile;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }
    public ConfigFile(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDirectoryFile() {
        return directoryFile;
    }

    public void setDirectoryFile(String directoryFile) {
        this.directoryFile = directoryFile;
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
}