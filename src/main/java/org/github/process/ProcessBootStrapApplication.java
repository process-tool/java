package org.github.process;

import org.github.process.choice.FTPOperation;
import org.github.process.config.Configuration;
import org.github.process.tool.impl.FtpProcess;
import org.github.process.tool.impl.ShellProcess;
import org.github.process.util.ShellThreadPool;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author : zhilin
 * @date : 2020/06/17
 */
@SpringBootApplication
public class ProcessBootStrapApplication {
    public static void main(String[] args) {
//        SpringApplication.run(ProcessBootStrapApplication.class);

        new ProcessBootStrap().stream()
                .upLoadFile("D:\\","test.jar","/home","lzl.jar")
                .command("nohup java -jar /home/lzl.jar > /home/a.log 2>&1 &")
                .execute()
                .shutdown();

    }
}
