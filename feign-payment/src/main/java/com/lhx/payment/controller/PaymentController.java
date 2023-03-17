package com.lhx.payment.controller;

import com.lhx.pojo.Payment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description
 *
 * @author damon.liu
 * Date 2022-12-12 6:58
 */
@RestController
@RequestMapping(value = "/payment")
@RefreshScope
public class PaymentController {

    @Value("${payment.name}")
    private String paymentName;

    /**
     * 获取端口信息
     */
    @Value("${server.port}")
    private String port;


    @GetMapping("/{id}")
    public Payment payment(@PathVariable("id") Integer id) {
        String str = String.format("支付成功，payment{port:%s}{paymentName:%s}", port, paymentName);
        Payment payment = new Payment(id, str);
        System.out.println("str: " +str);
        return payment;
    }

    @GetMapping("/info/{id}")
    public Payment paymentInfo(@PathVariable("id") Integer id) {
        String str = String.format("支付成功，payment{port:%s}{paymentName:%s}", port, paymentName);
        Payment payment = new Payment(id, str);
        System.out.println("str: " +str);
        // 故意报错，尝试降级
        int i = 0;
        int j = 1/i;
        return payment;
    }

    @GetMapping("/entity/{id}")
    public ResponseEntity<Object> paymentEntity(@PathVariable("id") Integer id) {
        String str = String.format("支付成功，payment{port:%s}{paymentName:%s}", port, paymentName);
        Payment payment = new Payment(id, str);
        System.out.println("str: " +str);
        return ResponseEntity.ok(payment);
    }

}
