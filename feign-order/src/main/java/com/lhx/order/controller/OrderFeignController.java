package com.lhx.order.controller;


import com.lhx.order.service.PaymentClient;
import com.lhx.pojo.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feign/order")
public class OrderFeignController {

    @Autowired
    private PaymentClient paymentClient;

    @GetMapping("/payment/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable("id") Integer id) {
        return paymentClient.payment(id);
    }

}
