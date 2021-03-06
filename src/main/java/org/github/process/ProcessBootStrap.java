package org.github.process;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.github.process.exception.ProcessException;
import org.github.process.thread.DefaultFactory;
import org.github.process.tool.abs.AbstractProcess;
import org.github.process.tool.impl.ShellProcess;
import org.github.process.util.ShellThreadPool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;


/**
 * 启动类
 *
 * @author : zhilin
 * @date : 2020/06/17
 */
@Slf4j
public class ProcessBootStrap {
    /**
     * 流程存放阻塞式列队
     */
    private final LinkedBlockingQueue<AbstractProcess> queue = new LinkedBlockingQueue<AbstractProcess>();

    /**
     * 流程个数
     */
    private Integer processSize = 0;

    /**
     * 整体配置项
     */
    private Properties properties;

    /**
     * 执行线程池
     */
    private final ThreadPoolExecutor singleThreadExecutor = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new DefaultFactory());

    /**
     * Shell线程池
     */
    private ShellThreadPool shellThreadPool;

    public ProcessBootStrap() {
        init();
    }

    private void init()  {
        //初始化配置
        this.properties = new Properties();
        try {
            properties.load(new FileInputStream(new File("src\\main\\resources\\process.properties")));
        }catch (Exception e){
            log.warn("读取本地配置失败");
        }
        //初始化shell
        if (this.properties.containsKey("ssh.host")) {
            String host = this.properties.getProperty("ssh.host");
            int port = Integer.valueOf(this.properties.getProperty("ssh.port"));
            String username = this.properties.getProperty("ssh.username");
            String password = this.properties.getProperty("ssh.password");
            this.shellThreadPool = new ShellThreadPool(host, port, username, password);
        }
    }

    /**
     * 添加流程
     *
     * @param process 流程实现类
     * @return this
     */
    public ProcessBootStrap addProcess(AbstractProcess process) {
        queue.offer(process);
        return this;
    }

    /**
     * 添加配置
     *
     * @param properties
     * @return
     */
    public ProcessBootStrap setConfig(Properties properties) {
        this.properties = properties;
        return this;
    }

    public ProcessBootStrap setConfig(String key,String value) {
        this.properties.setProperty(key, value);
        return this;
    }

    public ProcessBootStrap setConfig(Map<String,String> map) {
        this.properties.putAll(map);
        return this;
    }


    /**
     * 执行流程
     */
    public ProcessBootStrap execute() {
        try {
            while (queue.size() != 0) {
                AbstractProcess process = queue.poll();
                Future<Boolean> result = singleThreadExecutor.submit(process);
                Boolean aBoolean = result.get();
                log.info("{} process result is {}", process.getClass().getName(), aBoolean);
            }
        } catch (Exception e) {
            throw new ProcessException(e.getMessage());
        } finally {
            shutdown();
        }
        return this;
    }

    /**
     * 内置操作类
     * 内定了一些操作
     *
     * @return
     */
    public Operation stream() {
        return new Operation(queue, this);
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        singleThreadExecutor.shutdown();
        if (shellThreadPool != null) {
            shellThreadPool.close();
        }
    }

    /**
     * 内定操作类，定义了一些默认操作
     */
    public class Operation {
        private LinkedBlockingQueue<AbstractProcess> queue;
        private ProcessBootStrap processBootStrap;

        public Operation(LinkedBlockingQueue<AbstractProcess> queue, ProcessBootStrap processBootStrap) {
            this.queue = queue;
            this.processBootStrap = processBootStrap;
        }

        /**
         * 上传文件
         *
         * @param sourcePath     文件路径
         * @param sourceFileName 文件名
         * @param targetPath     目标路径
         * @param targetFileName 目标文件名
         * @return
         */
        public Operation upLoadFile(String sourcePath, String sourceFileName, String targetPath, String targetFileName) {
            AbstractProcess process = new AbstractProcess() {
                @Override
                public boolean invoke() throws Exception {
                    if (shellThreadPool == null) {
                        log.info("请配置ssh地址");
                        return false;
                    }
                    shellThreadPool.uploadFile(sourcePath, sourceFileName, targetPath, targetFileName);
                    return true;
                }
            };
            queue.offer(process);
            return this;
        }

        /**
         * 执行单个命令
         *
         * @param command command
         * @return
         */
        public Operation command(String command) {
            AbstractProcess process = new AbstractProcess() {
                @Override
                public boolean invoke() throws Exception {
                    if (shellThreadPool == null) {
                        log.info("请配置ssh地址");
                        return false;
                    }
                    shellThreadPool.command(command);
                    return true;
                }
            };
            queue.offer(process);
            return this;
        }

        /**
         * 执行多个命令
         *
         * @param commands commands
         * @return
         */
        public Operation commands(String... commands) {
            AbstractProcess process = new AbstractProcess() {
                @Override
                public boolean invoke() throws Exception {
                    if (shellThreadPool == null) {
                        log.info("请配置ssh地址");
                        return false;
                    }
                    shellThreadPool.commands(commands);
                    return true;
                }
            };
            queue.offer(process);
            return this;
        }

        /**
         * 内置执行
         *
         * @return
         */
        public ProcessBootStrap execute() {
            processBootStrap.execute();
            return processBootStrap;
        }
    }
}
