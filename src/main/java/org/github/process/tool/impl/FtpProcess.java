package org.github.process.tool.impl;

import org.github.process.choice.FTPOperation;
import org.github.process.config.Configuration;
import org.github.process.tool.abs.AbstractProcess;

import java.io.IOException;

/**
 * @author : zhilin
 * @date : 2020/06/17
 */
public class FtpProcess extends AbstractProcess {

    private String source;

    private String target;

    private Integer operation;

    public FtpProcess(String source, String target, FTPOperation operation) {
        this.source = source;
        this.target = target;
        this.operation = operation.getCode();
    }


    @Override
    public boolean invoke() throws Exception {



        return true;
    }

    /**
     * 上传文件
     */
    public void uploadFile(String source, String target) {

    }

    /**
     * 上传文件夹
     */
    public void uploadDir(String source, String target) {

    }

    /**
     * 下载文件
     */
    public void downloadFile(String source, String target) {

    }

    public void downloadDir(String source, String target) {

    }
}
