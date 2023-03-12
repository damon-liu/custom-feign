package com.lhx.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Description
 *
 * @author damon.liu
 * Date 2022-12-12 6:40
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    /**
     * 订单编号
     */
    private Integer id;
    /**
     * 支付状态
     */
    private String message;
}
