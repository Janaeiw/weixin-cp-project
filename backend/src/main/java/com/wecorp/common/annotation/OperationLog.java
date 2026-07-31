package com.wecorp.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解，用于标注在 Controller 方法上，
 * 补充模块名和操作描述。未标注时 AOP 也会自动记录。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /** 模块名 */
    String module() default "";

    /** 操作描述 */
    String operation() default "";
}
