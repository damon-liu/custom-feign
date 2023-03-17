package com.custom.feign.exception;

/**
 * Description:
 *
 * @author damon.liu
 * Date 2023-03-14 14:35
 */
public class CustomFeignException extends RuntimeException{

    private Integer code;

    private String msg;

    public CustomFeignException(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public CustomFeignException(String message) {
        super(message);
    }

}
