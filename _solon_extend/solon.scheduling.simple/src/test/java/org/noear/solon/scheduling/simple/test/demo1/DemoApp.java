package org.noear.solon.scheduling.simple.test.demo1;

import org.noear.solon.Solon;
import org.noear.solon.scheduling.annotation.EnableScheduling;

/**
 * @author noear 2022/10/15 created
 */
@EnableScheduling
public class DemoApp {
    public static void main(String[] args){
        Solon.start(DemoApp.class, args);
    }
}
