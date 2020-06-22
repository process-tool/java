package org.github.process.tool;

/**
 * 工具接口，该接口实现类可以注入ProcessBootStrap
 *
 * @author : zhilin
 * @date : 2020/06/17
 */
public interface Process {

    /**
     * 该方法会自动调用，根据不同业务调用具体的方法
     * @return true false
     */
    boolean invoke() throws Exception;
}
