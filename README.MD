1. 如果要发布到linux,配置文件,resources目录下配置process.properties
```
ssh.host = 192.168.66.168
ssh.port = 22
ssh.username = root
ssh.password = root
```


2. 将项目发布到linux并且运行
```
new ProcessBootStrap().stream()
                .upLoadFile("D:\\", "test.jar", "/home", "test.jar")
                .command("nohup java -jar /home/test.jar > /home/a.log 2>&1 &")
                .execute()
                .shutdown();
```