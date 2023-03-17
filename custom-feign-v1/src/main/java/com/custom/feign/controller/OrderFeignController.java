package com.custom.feign.controller;


import com.custom.feign.service.TestFeignClient;
import com.lhx.pojo.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/custom/feign/order")
public class OrderFeignController {

    @Autowired
    private TestFeignClient testClient;

    @RequestMapping("/payment/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable("id") Integer id) {
        return new ResponseEntity(testClient.getPaymentById(id), HttpStatus.OK);
    }

    @RequestMapping("/payment/info/{id}")
    public ResponseEntity<Payment> getPaymentInfo(@PathVariable("id") Integer id) {
        return new ResponseEntity(testClient.getPaymentInfo(id), HttpStatus.OK);
    }


}
