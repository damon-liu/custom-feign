package com.custom.feign.core.anno;

import com.custom.feign.core.CustomFeignClientsRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Description:
 *
 * @author damon.liu
 * Date 2023-03-10 6:18
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({CustomFeignClientsRegistrar.class})
public @interface CustomEnableFeignClients {

    String[] value() default {};

    String[] basePackages() default {};

    Class<?>[] basePackageClasses() default {};

    Class<?>[] defaultConfiguration() default {};

    Class<?>[] clients() default {};
}
