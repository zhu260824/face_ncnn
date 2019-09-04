package com.zl.aliyun.util.http;

import java.io.File;
import java.io.Serializable;

/**
 * @author zl
 * @Version 1.0
 * @Description TODO
 * @date 2018/11/02  10:21
 */
public class FileContentFile implements Serializable {
    private static final long serialVersionUID = -6339050941229672951L;
    private String reqName;
    private String fileName;
    private File file;

    public FileContentFile() {
    }

    public FileContentFile(String reqName, String fileName, File file) {
        this.reqName = reqName;
        this.fileName = fileName;
        this.file = file;
    }

    public String getReqName() {
        return reqName;
    }

    public void setReqName(String reqName) {
        this.reqName = reqName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return "FileContentFile{" +
            "reqName='" + reqName + '\'' +
            ", fileName='" + fileName + '\'' +
            ", file=" + file.getAbsolutePath() +
            '}';
    }
}
