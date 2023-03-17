package com.custom.feign.core;

import org.springframework.beans.factory.FactoryBean;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Description:
 *
 * @author damon.liu
 * Date 2023-03-13 3:17
 */
public class CustomFeignBeanFactory implements FactoryBean {

    private Class<?> clazz;

    private InvocationHandler handler;



    public CustomFeignBeanFactory(Class<?> clazz, InvocationHandler handler) {
        this.clazz = clazz;
        this.handler = handler;
    }


    @Override
    public Object getObject() {
        return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, handler);
    }

    @Override
    public Class<?> getObjectType() {
        return clazz;
    }
}
