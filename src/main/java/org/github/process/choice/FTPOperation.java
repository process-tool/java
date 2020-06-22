package org.github.process.choice;

import lombok.Data;

/**
 * @author : zhilin
 * @date : 2020/06/19
 */
public enum  FTPOperation {
    /**
     *上传文件
     */
    UPLOAD_FILE(0),
    /**
     * 上传文件夹
     */
    UPLOAD_DIR(1),
    /**
     * 下载文件
     */
    DOWNLOAD_FILE(2),
    /**
     * 下载文件夹
     */
    DOWNLOAD_DIR(3);

    private Integer code;

    FTPOperation(Integer code){
        this.code = code;
    }

    public Integer getCode(){
        return this.code;
    }
}
