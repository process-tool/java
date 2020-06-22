package org.github.process.tool.impl;

import org.github.process.tool.abs.AbstractProcess;

/**
 * @author : zhilin
 * @date : 2020/06/17
 */
public class ShellProcess extends AbstractProcess {
    //////////////////////////////
    //       配置信息            //
    /////////////////////////////


    @Override
    public boolean invoke() throws Exception {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("shell process");
        return true;
    }
}
