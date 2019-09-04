package com.zl.aliyun.util.http;

import java.io.Serializable;

/**
 * @author ZL
 */
public final class ByteContentFile implements Serializable {
    private static final long serialVersionUID = 127254956313902461L;
    private String reqName;
    private String fileName;
    private byte[] content;

    public ByteContentFile() {
    }

    public ByteContentFile(String reqName, String fileName, byte[] content) {
        this.reqName = reqName;
        this.fileName = fileName;
        this.content = content;
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

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ByteContentFile{" +
            "reqName='" + reqName + '\'' +
            ", fileName='" + fileName + '\'' +
            ", content=" + content.length +
            '}';
    }
}
