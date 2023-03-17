package com.custom.feign.core.anno;


import com.custom.feign.core.inter.TokenHeaderInter;

import java.lang.annotation.*;

/**
 * @author User
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CustomFeignHeader {
    /**
     * 服务名 或者是 url地址
     * @return
     */
    Class<? extends TokenHeaderInter> value();
    /**
     * 是否打印结果
     * @return
     */
    boolean logResult() default true;
    /**
     * 日志最大打印长度
     * @return
     */
    int logMaxLen() default 1000;
}
