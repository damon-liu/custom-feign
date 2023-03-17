package com.custom.feign.core.anno;

import com.custom.feign.core.inter.TokenHeaderDefault;
import com.custom.feign.core.inter.TokenHeaderInter;

import java.lang.annotation.*;


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CustomFeignClient {

    /**
     * 注册bean名称，默认类名
     *
     * @return
     */
    String name() default "";

    /**
     * 调用url
     *
     * @return
     */
    String url();

    Class<? extends TokenHeaderInter> tokenHeader() default TokenHeaderDefault.class;

    Class<?> fallback() default void.class;
}
