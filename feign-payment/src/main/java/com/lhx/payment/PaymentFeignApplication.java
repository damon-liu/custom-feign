package com.lhx.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Description
 *
 * @author damon.liu
 * Date ${DATE} ${TIME}
 */

@SpringBootApplication
@EnableDiscoveryClient
public class PaymentFeignApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentFeignApplication.class, args);
    }
}