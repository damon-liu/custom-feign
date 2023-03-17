package com.custom.feign.service;

import com.custom.feign.core.anno.CustomFeignClient;
import com.lhx.pojo.Payment;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Description:
 *
 * @author damon.liu
 * Date 2023-03-12 15:00
 */
@CustomFeignClient(url = "http://127.0.0.1:8080", fallback = TestFeignClient.Fallback.class)
@RequestMapping(value = "/payment")
public interface TestFeignClient {

    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    Payment getPaymentById(@PathVariable("id") int id);

    @RequestMapping(value = "/info/{id}",method = RequestMethod.GET)
    Payment getPaymentInfo(@PathVariable("id") int id);

    class Fallback implements TestFeignClient {

        @Override
        public Payment getPaymentById(int id) {
            return new Payment(id, "getPaymentById熔断降级方法返回");
        }

        @Override
        public Payment getPaymentInfo(int id) {
            return new Payment(id, "getPaymentInfo熔断降级方法返回");
        }
    }
}
