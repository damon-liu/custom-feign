package com.custom.feign;

import com.custom.feign.core.anno.CustomEnableFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@CustomEnableFeignClients(basePackages = "com.custom.feign.service")
public class CustomFeignApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomFeignApplication.class, args);
    }

}
