package org.github.process.thread;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author : zhilin
 * @date : 2020/06/29
 */
@Slf4j
public class FileCompleteListener implements Runnable {
    private AtomicLong count;
    private long all;

    public FileCompleteListener(AtomicLong count, long all) {
        this.count = count;
        this.all = all;
    }

    @SneakyThrows
    @Override
    public void run() {
        while (count.get()!=all){
            double value = (double) count.get() / all;
            DecimalFormat format = new DecimalFormat("##.##%");
            String result = format.format(value);
            log.info("上传度为: {}",result);
            TimeUnit.MICROSECONDS.sleep(800);
        }
    }
}
