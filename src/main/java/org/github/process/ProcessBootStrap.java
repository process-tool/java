package org.github.process;

import lombok.extern.slf4j.Slf4j;
import org.github.process.config.Configuration;
import org.github.process.exception.ProcessException;
import org.github.process.thread.DefaultFactory;
import org.github.process.tool.abs.AbstractProcess;
import org.github.process.tool.impl.ShellProcess;
import org.github.process.util.ShellThreadPool;

import java.util.Properties;
import java.util.concurrent.*;

import static org.github.process.config.Configuration.*;


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
    private LinkedBlockingQueue<AbstractProcess> queue = new LinkedBlockingQueue<AbstractProcess>();

    private Integer processSize = 0;

    private Properties properties ;

    /**
     * 执行线程池
     */
    private ThreadPoolExecutor singleThreadExecutor = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new DefaultFactory());

    /**
     * Shell线程池
     */
    private ShellThreadPool shellThreadPool = new ShellThreadPool(FTP_HOST,FTP_PORT,FTP_USERNAME,FTP_PASSWORD);

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
     * @param properties
     * @return
     */
    public ProcessBootStrap setConfig(Properties properties){
        this.properties = properties;
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
     * @return
     */
    public Operation stream(){
        return new Operation(queue,this);
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        singleThreadExecutor.shutdown();
        shellThreadPool.close();
    }

    /**
     * 内定操作类
     */
    public class Operation{
        private LinkedBlockingQueue<AbstractProcess> queue;
        private ProcessBootStrap processBootStrap;

        public Operation(LinkedBlockingQueue<AbstractProcess> queue,ProcessBootStrap processBootStrap) {
            this.queue = queue;
            this.processBootStrap = processBootStrap;
        }

        /**
         * 上传文件
         * @param sourcePath 文件路径
         * @param sourceFileName 文件名
         * @param targetPath 目标路径
         * @param targetFileName 目标文件名
         * @return
         */
        public Operation upLoadFile(String sourcePath,String sourceFileName,String targetPath,String targetFileName){
            AbstractProcess process = new AbstractProcess() {
                @Override
                public boolean invoke() throws Exception {
                    shellThreadPool.uploadFile(sourcePath, sourceFileName, targetPath, targetFileName);
                    return true;
                }
            };
            queue.offer(process);
            return this;
        }

        /**
         * 执行单个命令
         * @param command command
         * @return
         */
        public Operation command(String command){
            AbstractProcess process = new AbstractProcess() {
                @Override
                public boolean invoke() throws Exception {
                    shellThreadPool.command(command);
                    return true;
                }
            };
            queue.offer(process);
            return this;
        }

        /**
         * 执行多个命令
         * @param commands commands
         * @return
         */
        public Operation commands(String... commands){
            AbstractProcess process = new AbstractProcess() {
                @Override
                public boolean invoke() throws Exception {
                    shellThreadPool.commands(commands);
                    return true;
                }
            };
            queue.offer(process);
            return this;
        }

        /**
         * 内置执行
         * @return
         */
        public ProcessBootStrap execute(){
            processBootStrap.execute();
            return processBootStrap;
        }
    }
}
