package com.custom.feign.core.invocation;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.custom.feign.core.anno.CustomFeignClient;
import com.custom.feign.core.anno.CustomFeignHeader;
import com.custom.feign.core.inter.TokenHeaderInter;
import com.custom.feign.exception.CustomFeignException;
import com.custom.feign.util.OkHttpUtils;
import com.custom.feign.util.PlaceholderResolver;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Description:
 *
 * @author damon.liu
 * Date 2023-03-14 7:56
 */
@Slf4j
public class OkHttpHttpExecute implements InvocationHandler {

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    private String url;

    private Class<?> fallbackClazz;

    private final BeanFactory beanFactory;

    public OkHttpHttpExecute(String url, BeanFactory beanFactory, Class<?> fallbackClazz) {
        this.url = url;
        this.beanFactory = beanFactory;
        this.fallbackClazz = fallbackClazz;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        CustomFeignClient iceRpcClient = AnnotationUtils.findAnnotation(method.getDeclaringClass(), CustomFeignClient.class);
        RequestMapping requestMapping = AnnotationUtils.findAnnotation(method, RequestMapping.class);
        if (Objects.isNull(requestMapping)) {
            String errorInfo = method.getDeclaringClass().getName() + "#" + method.getName();
            throw new CustomFeignException("未找到方法： " + errorInfo);
        }
        RequestMethod requestMethod = requestMapping.method()[0];
        String[] uri = requestMapping.value();
        StringBuilder urlBuilder = new StringBuilder(url).append(uri[0]);
        //获取方法参数的数据
        int count = method.getParameterCount();
        Class<?>[] parameterTypes = method.getParameterTypes();
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        Map<String, Object> paramMap = Maps.newHashMap();
        Map<String, Object> bodyMap = Maps.newHashMap();
        Map<String, Object> pathMap = Maps.newHashMap();
        CustomFeignHeader rpcHeader = AnnotationUtils.findAnnotation(method, CustomFeignHeader.class);
        Map<String, String> headers = this.getHeader(iceRpcClient, rpcHeader, requestMapping.headers());
        for (int i = 0; i < count; i++) {
            Class<?> parameterType = parameterTypes[i];
            //获取方法参数上面的注解
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            if (parameterAnnotations.length < 1) {
                Object body = args[i];
                JSONObject jsonObject = (JSONObject) JSONObject.toJSON(body);
                paramMap.putAll(jsonObject);
                continue;
            }
            Annotation annotation = parameterAnnotations[i][0];
            if (annotation instanceof ModelAttribute) {
                Object body = args[i];
                if (body instanceof Map) {
                    paramMap.putAll((Map) body);
                } else {
                    JSONObject jsonObject = (JSONObject) JSONObject.toJSON(body);
                    paramMap.putAll(jsonObject);
                }
                continue;
            }
            if (annotation instanceof RequestParam) {
                RequestParam requestParam = (RequestParam) annotation;
                String value = requestParam.value();
                String requestParamName = StringUtils.hasText(value) ? value : parameterNames[i];
                String requestParamValue = String.class.equals(parameterType) ? (String) args[i] : String.valueOf(args[i]);
                paramMap.put(requestParamName, requestParamValue);
                continue;
            }
            if (annotation instanceof PathVariable) {
                PathVariable pathVariable = (PathVariable) annotation;
                String value = pathVariable.value();
                String requestParamName = StringUtils.hasText(value) ? value : parameterNames[i];
                String requestParamValue = String.class.equals(parameterType) ? (String) args[i] : String.valueOf(args[i]);
                pathMap.put(requestParamName, requestParamValue);
                continue;
            }
            if (annotation instanceof RequestBody) {
                Object body = args[i];
                JSONObject jsonObject = (JSONObject) JSONObject.toJSON(body);
                paramMap.putAll(jsonObject);
            }
        }
        // 路径填值
        if (!pathMap.isEmpty()) {
            PlaceholderResolver placeholderResolver = PlaceholderResolver.getResolver("{", "}");
            urlBuilder = new StringBuilder(placeholderResolver.resolveByMap(urlBuilder.toString(), pathMap));
        }

        // 如果验证为参数key类型的
        // Class<? extends RpcApiKeyInter> apiKeyClazz = iceRpcClient.apiKey();
        // if (apiKeyClazz != null && !apiKeyClazz.getSimpleName().equals(RpcApiKeyDefault.class.getSimpleName())) {
        //     RpcApiKeyInter apiKeyInter = SpringUtil.getBean(apiKeyClazz);
        //     if(urlBuilder.indexOf("?") > 0){
        //         urlBuilder.append("&").append(apiKeyInter.apiKey()).append("=").append(apiKeyInter.apiKeyVal(iceRpcClient.apiKeyValParam()));
        //     }else{
        //         urlBuilder.append("?").append(apiKeyInter.apiKey()).append("=").append(apiKeyInter.apiKeyVal(iceRpcClient.apiKeyValParam()));
        //     }
        // }

        String url = urlBuilder.toString();
        OkHttpUtils httpUtils = beanFactory.getBean(OkHttpUtils.class);
        log.debug("RPC 请求 url：[{}] {} -> 入参：{} header:{}", requestMethod, url, JSON.toJSONString(paramMap), headers);
        Response response = null;
        IOException exception = null;
        try {
            response = httpUtils.requestRes(url, paramMap, headers, httpMethod(requestMethod));
        } catch (IOException e) {
            exception = e;
            Object fallbackObj = fallbackClazz.newInstance();
            Method fallbackMethod = fallbackObj.getClass().getMethod(method.getName(), method.getParameterTypes());
            return fallbackMethod.invoke(fallbackObj, args);
        } finally {
            if (response != null && exception != null) {
                response.close();
            }
        }
        try {
            String jsonRes = response.body().string();
            if (response.isSuccessful()) {
                log.debug("耗时：{}ms,RPC 接口响应结果：{}", response.receivedResponseAtMillis() - response.sentRequestAtMillis(), jsonRes);
                Class<?> returnType = method.getReturnType();
                Class<?> actualType = null;
                if (returnType == List.class) {
                    Type type = method.getGenericReturnType();
                    if (type instanceof ParameterizedType) {
                        Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
                        //因为list泛型只有一个值 所以直接取0下标
                        String typeName = actualTypeArguments[0].getTypeName();
                        //真实返回值类型 Class对象
                        actualType = Class.forName(typeName);
                    }
                    return JSONObject.parseArray(jsonRes, actualType);
                } else {
                    return JSONObject.parseObject(jsonRes, returnType);
                }
            } else {
                log.error("RPC 接口响应 返回异常：Http status：{} -> {} ->{}", response.code(), response.message(), jsonRes);
                log.error("RPC 请求 url：[{}] {} -> 入参：{} header:{}", requestMethod, url, JSON.toJSONString(paramMap), headers);
                throw new CustomFeignException(response.code(), "远程调用异常！");
            }
        } finally {
            response.close();
        }
    }


    private Map<String, String> getHeader(CustomFeignClient iceRpcClient, CustomFeignHeader annotation, String[] headers) {
        Map<String, String> map = new HashMap<>();
        // 默认
        map.put(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
        for (String header : headers) {
            String[] headerArray = header.split("=");
            map.put(headerArray[0], headerArray[1]);
        }
        Class<? extends TokenHeaderInter> clazz = iceRpcClient.tokenHeader();
        if (annotation != null) {
            clazz = annotation.value();
        }
        if (clazz != null) {
            TokenHeaderInter tokenHeaderInter = SpringUtil.getBean(clazz);
            if (tokenHeaderInter == null) {
                return map;
            }
            map.putAll(tokenHeaderInter.tokenHeader());
        }
        return map;
    }

    private OkHttpUtils.HTTPMethod httpMethod(RequestMethod requestMethod) {
        switch (requestMethod) {
            case POST:
                return OkHttpUtils.HTTPMethod.POST;
            case GET:
                return OkHttpUtils.HTTPMethod.GET;
            case DELETE:
                return OkHttpUtils.HTTPMethod.DELETE;
            case PUT:
                return OkHttpUtils.HTTPMethod.PUT;
            case PATCH:
                return OkHttpUtils.HTTPMethod.PATCH;
        }
        return null;
    }


}
