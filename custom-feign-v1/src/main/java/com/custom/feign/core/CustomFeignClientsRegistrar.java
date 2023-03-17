package com.custom.feign.core;

import com.custom.feign.core.anno.CustomEnableFeignClients;
import com.custom.feign.core.anno.CustomFeignClient;
import com.custom.feign.core.invocation.OkHttpHttpExecute;
import com.custom.feign.core.inter.TokenHeaderDefault;
import com.custom.feign.util.OkHttpUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.*;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Description:
 *
 * @author damon.liu
 * Date 2023-03-13 7:51
 */
@Log4j2
public class CustomFeignClientsRegistrar implements ImportBeanDefinitionRegistrar, BeanFactoryAware, EnvironmentAware, ResourceLoaderAware, BeanClassLoaderAware {

    private ResourceLoader resourceLoader;

    private ClassLoader classLoader;

    private Environment environment;

    private BeanFactory beanFactory;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        // 获取@CustomEnableFeignClients扫描的包路径
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(CustomEnableFeignClients.class.getName()));
        Set<String> basePackages = new HashSet<>();
        for (String pkg : (String[]) attributes.get("basePackages")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }

        // 将TokenHeaderDefault、OkHttpUtils注入spring容器
        RootBeanDefinition rootBeanDefinitionDefault = new RootBeanDefinition(TokenHeaderDefault.class);
        registry.registerBeanDefinition(TokenHeaderDefault.class.getSimpleName(), rootBeanDefinitionDefault);
        RootBeanDefinition httpToolBeanDefinition = new RootBeanDefinition(OkHttpUtils.class);
        registry.registerBeanDefinition(OkHttpUtils.class.getSimpleName(), httpToolBeanDefinition);

        // 扫描配置的包路径下的BeanDefinition
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(this.resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(CustomFeignClient.class));
        for (String basePackage : basePackages) {
            Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
            for (BeanDefinition candidateComponent : candidateComponents) {
                if (candidateComponent instanceof AnnotatedBeanDefinition) {
                    // 校验@CustomFeignClient注释的必须是一个interface
                    AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                    AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
                    Assert.isTrue(annotationMetadata.isInterface(), "@CustomFeignClient注解的必须是一个interface");
                    // 获取CustomFeignClient接口元数据map
                    Map<String, Object> attributeMap = annotationMetadata.getAnnotationAttributes(CustomFeignClient.class.getCanonicalName());
                    if (CollectionUtils.isEmpty(attributeMap)) {
                        return;
                    }
                    // 反射生成接口实现类
                    String className = annotationMetadata.getClassName();
                    Class<?> clazz = null;
                    try {
                        clazz = Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        log.error("httpClient start up fail:", e);
                    }
                    // 拼接请求url
                    RequestMapping baseRequest = clazz.getAnnotation(RequestMapping.class);
                    String[] value = baseRequest.value();
                    String feignBaseUrl = value.length > 0 ? value[0] : "";
                    String url = attributeMap.get("url") + "/" + feignBaseUrl;
                    // 封装BeanDefinition
                    Class<?> fallbackClazz = (Class<?>) attributeMap.get("fallback");
                    String beanName = className.substring(className.lastIndexOf(".") + 1);
                    String alias = beanName.substring(0, 1).toLowerCase().concat(beanName.substring(1)).concat("HttpClient");
                    BeanDefinitionBuilder definition = BeanDefinitionBuilder.genericBeanDefinition(CustomFeignBeanFactory.class);
                    definition.addConstructorArgValue(clazz);
                    definition.addConstructorArgValue(new OkHttpHttpExecute(url, beanFactory, fallbackClazz));
                    definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
                    AbstractBeanDefinition handleDefinition = definition.getBeanDefinition();
                    handleDefinition.setPrimary(true);

                    // 向Spring注册bean组件
                    BeanDefinitionHolder holder = new BeanDefinitionHolder(handleDefinition, className, new String[]{alias});
                    BeanDefinitionReaderUtils.registerBeanDefinition(holder, registry);
                }
            }
        }
    }

    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {

            @Override
            protected boolean isCandidateComponent(
                    AnnotatedBeanDefinition beanDefinition) {
                if (beanDefinition.getMetadata().isIndependent()) {
                    if (beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().getInterfaceNames().length == 1
                            && Annotation.class.getName().equals(beanDefinition.getMetadata().getInterfaceNames()[0])) {
                        try {
                            Class<?> target = ClassUtils.forName(beanDefinition.getMetadata().getClassName(), CustomFeignClientsRegistrar.this.classLoader);
                            return !target.isAnnotation();
                        } catch (Exception ex) {
                            this.logger.error("Could not load target class: " + beanDefinition.getMetadata().getClassName(), ex);
                        }
                    }
                    return true;
                }
                return false;
            }
        };
    }
}
