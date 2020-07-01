package org.github.process.util;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.github.process.thread.FileCompleteListener;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhilin
 * @Description: shell线程池
 * @time 2020/4/17
 */
@Slf4j
public class ShellThreadPool {
    //    private ThreadPoolExecutor pool;
    private String IP;
    private Integer PORT;
    private String USERNAME;
    private String PASSWORD;
    private Session sshSession;
    private ExecutorService pool;

    //初始化线程池和shell连接
    public ShellThreadPool(String ip, int port, String username, String password) {
        this.IP = ip;
        this.PORT = port;
        this.USERNAME = username;
        this.PASSWORD = password;
        initThreadPool();
        log.info("线程池初始化成功");
        initSSH();
        log.info("SSH客户端初始化成功");
    }

    /**
     * 初始化线程池
     */
    void initThreadPool() {
        this.pool = new ThreadPoolExecutor(5, 10, 10,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(5));
//        this.pool = Executors.newFixedThreadPool(3);
    }

    /**
     * 初始化SSH客户端
     */
    void initSSH() {
        JSch jsch = new JSch();
        Session session = null;
        try {
            //创建session并且打开连接，因为创建session之后要主动打开连接
            if (PORT == 0) {
                session = jsch.getSession(USERNAME, IP, PORT);
            } else {
                session = jsch.getSession(USERNAME, IP, PORT);
            }

            session.setPassword(PASSWORD);

            //关闭主机密钥检查，否则会导致连接失败，重要！！！
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            log.info("连接服务器" + session.getHost());

            session.connect();
            this.sshSession = session;

        } catch (Exception e) {
            throw new RuntimeException("初始化ssh失败");
        }
    }


    /**
     * 命令执行
     *
     * @param command 命令
     * @return
     */
    public List<String> command(String command) {
        long start = System.currentTimeMillis();

        //创建任务
        Callable<List<String>> call = new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                ArrayList<String> stdout = new ArrayList<>();
                //打开通道，设置通道类型，和执行的命令
                Channel channel = sshSession.openChannel("exec");
                ChannelExec channelExec = (ChannelExec) channel;
                channelExec.setCommand(command);

                channelExec.setInputStream(null);
                BufferedReader input = new BufferedReader(new InputStreamReader((channelExec.getInputStream())));
                channelExec.connect();
                log.info("The remote command is:" + command);
                //接受远程服务器执行命令的结果

                String line = null;
                log.info("stdout信息开始打印");
                while ((line = input.readLine()) != null) {
                    stdout.add(line);
//              log.info(line);
                }
                log.info("stdout信息打印结束");
                input.close();

                //关闭通道
                channelExec.disconnect();
                return stdout;
            }
        };
        //创建结果接收
        Future<List<String>> future = this.pool.submit(call);
        try {
            boolean flag = true;
            while (flag) {
                //线程完成并且不是被取消
                if (future.isDone() && !future.isCancelled()) {
                    long id = Thread.currentThread().getId();
                    System.out.println("线程 :  " + id + "号 执行完毕");
                    flag = false;
                }
            }
            List<String> strings = future.get();
            long end = System.currentTimeMillis();
            System.out.println("花费时间:  " + (end - start) / 1000);
            return strings;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("future获取出错");
        }

    }


    /**
     * shell交互模式
     * 多条命令执行，某些特殊的系统不能使用逗号分割命令，只有一行一行的执行
     *
     * @param commands
     * @return
     */
    public List<String> commands(String... commands) {
        Callable<List<String>> call = new Callable<List<String>>() {
            @Override
            public List<String> call() throws Exception {
                ArrayList<String> list = new ArrayList<>();
                String result = "";

                //2.尝试解决 远程ssh只能执行一句命令的情况
                ChannelShell channelShell = (ChannelShell) sshSession.openChannel("shell");
                InputStream inputStream = channelShell.getInputStream();//从远端到达的数据  都能从这个流读取到
                channelShell.setPty(true);
                channelShell.connect();

                OutputStream outputStream = channelShell.getOutputStream();//写入该流的数据  都将发送到远程端
                //使用PrintWriter 就是为了使用println 这个方法
                //好处就是不需要每次手动给字符加\n
                PrintWriter printWriter = new PrintWriter(outputStream);
                for (String command : commands) {
                    printWriter.println(command);
                }
                printWriter.println("exit");//为了结束本次交互
                printWriter.flush();//把缓冲区的数据强行输出

                /**
                 shell管道本身就是交互模式的。要想停止，有两种方式：
                 一、人为的发送一个exit命令，告诉程序本次交互结束
                 二、使用字节流中的available方法，来获取数据的总大小，然后循环去读。
                 为了避免阻塞
                 */
                BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));
                String line = null;
                while ((line = input.readLine()) != null) {
                    list.add(line);
                }
                outputStream.close();
                inputStream.close();
                channelShell.disconnect();
                log.info("shell mode done");
                return list;
            }
        };
        //创建结果接收
        Future<List<String>> future = this.pool.submit(call);
        try {
            boolean flag = true;
            while (flag) {
                //线程完成并且不是被取消
                if (future.isDone() && !future.isCancelled()) {
                    long id = Thread.currentThread().getId();
                    flag = false;
                }
            }
            List<String> strings = future.get();
            return strings;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("future获取出错");
        }
    }

    /**
     * 上传文件
     * @param sourcePath  源文件路径  c://test
     * @param sourceFileName 源文件名 test.txt
     * @param targetPath 目标路径
     * @param targetFileName 目标文件名
     */
    public void uploadFile(String sourcePath,String sourceFileName,String targetPath,String targetFileName)  {
        String source = sourcePath +"/" + sourceFileName;
        String target = targetPath + "/" + targetFileName;
        ChannelSftp sftp = null;
        FileInputStream fileInputStream = null;
        try{
            // 获取sftp通道
            sftp = (ChannelSftp) sshSession.openChannel("sftp");
            sftp.connect();
            File file = new File(source);
            AtomicLong count = new AtomicLong(0);
            pool.submit(new FileCompleteListener(count,file.length()));
            fileInputStream = new FileInputStream(file);
            OutputStream outputStream = sftp.put(target);
            byte[] bytes = new byte[1024 * 1024];
            int n = 0;
            while ((n = fileInputStream.read(bytes,0,bytes.length))!= -1){
                outputStream.write(bytes,0,n);
                count.addAndGet(n);
            }
            outputStream.flush();
            outputStream.close();

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            sftp.disconnect();
            if(fileInputStream!=null){
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        log.info("文件上传成功: 100%");
    }

    /**
     *
     * @param sourcePath  源文件路径  /test
     * @param sourceFileName 源文件名 test.txt
     * @param targetPath 目标路径 /home
     * @param targetFileName 目标文件名 test.txt
     */
    public void downloadFile(String sourcePath,String sourceFileName,String targetPath,String targetFileName){
        ChannelSftp sftp = null;
        FileOutputStream outputStream = null;
        String source = sourcePath +"/" + sourceFileName;
        String target = targetPath + "/" + targetFileName;
        try{
            // 获取sftp通道
            sftp = (ChannelSftp) sshSession.openChannel("sftp");
            sftp.connect();
            File file = new File(targetPath);
            if(!file.exists()){
                file.mkdirs();
            }
            outputStream = new FileOutputStream(new File(target));
            sftp.get(source,outputStream);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            sftp.disconnect();
            if(outputStream!=null){
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        log.info("文件下载成功");
    }

    /**
     * 文件夹上传
     * @param sourcePath 源文件夹
     * @param targetPath 目标文件夹
     */
    public void uploadDir(String sourcePath,String targetPath){
        ChannelSftp sftp = null;
        try{
            sftp = (ChannelSftp) sshSession.openChannel("sftp");
            sftp.connect();
            upRecursion(sftp,sourcePath,targetPath);
            log.info("文件全部上传成功");
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            sftp.disconnect();
        }

    }

    private void upRecursion(ChannelSftp channelSftp,String sourcePath,String targetPath) throws IOException, SftpException {
        File root = new File(sourcePath);
        File[] files = root.listFiles();
        for (File file : files) {
            if(file.isFile()){
                FileInputStream fileInputStream = new FileInputStream(file);
                channelSftp.put(fileInputStream,targetPath+"/"+file.getName());
                fileInputStream.close();
            }else {
                upRecursion(channelSftp, file.getPath(), targetPath);
            }
        }
    }



    /**
     * 关闭连接池
     */
    public void close() {
        //关闭ssh连接
        sshSession.disconnect();
        //关闭线程池
        pool.shutdown();
    }
}