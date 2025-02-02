package org.noear.solon.test;

import org.noear.solon.annotation.Alias;
import org.noear.solon.annotation.Note;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SolonTest {
    @Note("启动类")
    @Alias("classes")
    Class<?> value() default Void.class;

    @Note("启动类")
    @Alias("value")
    Class<?> classes() default Void.class;

    @Note("延迟秒数")
    int delay() default 1;

    /**
     * 环境配置
     * */
    @Note("环境配置")
    String env() default "";

    /**
     * args（例：--app.name=demoapp）
     * */
    @Note("启动参数")
    String[] args() default {};

    /**
     * 应用属性（例：solon.app.name=demoapp）
     * */
    @Note("应用属性")
    String[] properties() default {};

    /**
     * 是否调试模式
     * */
    @Note("是否调试模式")
    boolean debug() default true;
}
