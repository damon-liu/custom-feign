package com.lhx.order.service;


import com.lhx.pojo.Payment;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @FeignClient 注解作用：声明一个FeignClient客户端，参数为当前消费者要调用的提供者的微服务名称
 * Feign客户端接口定义：
 * 1.该接口名可以随意
 * 2.接口方法名可以随意
 * 3.方法参数及返回值必须要与提供者端相应方法相同
 * 在当前接口中采用SpringMVC注解
 */
@FeignClient(value = "feign-payment", fallback = PaymentClient.Fallback.class)
@RequestMapping("/payment")
public interface PaymentClient {

    @GetMapping("/{id}")
    ResponseEntity<Payment> payment(@PathVariable("id") int id);

    @Component
    @RequestMapping("/fallback_payment")
    class Fallback implements PaymentClient {

        @Override
        public ResponseEntity<Payment> payment(int id) {
            Payment payment = new Payment(id, "熔断降级方法返回");
            return ResponseEntity.ok(payment);
        }
    }
}

