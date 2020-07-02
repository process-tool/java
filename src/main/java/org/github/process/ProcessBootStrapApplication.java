package org.github.process;

import lombok.SneakyThrows;
import org.github.process.choice.FTPOperation;
import org.github.process.thread.FileCompleteListener;
import org.github.process.tool.impl.FtpProcess;
import org.github.process.tool.impl.ShellProcess;
import org.github.process.util.ShellThreadPool;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.*;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author : zhilin
 * @date : 2020/06/17
 */
@SpringBootApplication
public class ProcessBootStrapApplication {

    public static void main(String[] args) throws IOException, InterruptedException {
//        SpringApplication.run(ProcessBootStrapApplication.class);

        new ProcessBootStrap().stream()
                .upLoadFile("D:\\", "test.jar", "/home", "test.jar")
                .command("nohup java -jar /home/test.jar > /home/a.log 2>&1 &")
                .execute()
                .shutdown();



    }
}
