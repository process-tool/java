package org.github.process.tool.abs;

import org.github.process.tool.Process;

import java.util.concurrent.Callable;

/**
 * tool 实现实际集成类
 * @author : zhilin
 * @date : 2020/06/18
 */
public abstract class AbstractProcess implements Callable<Boolean>, Process {
    @Override
    public Boolean call() throws Exception {
        return invoke();
    }
}
