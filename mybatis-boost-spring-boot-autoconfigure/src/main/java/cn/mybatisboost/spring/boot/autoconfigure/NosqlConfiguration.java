package cn.mybatisboost.spring.boot.autoconfigure;

import cn.mybatisboost.metric.MetricInterceptor;
import cn.mybatisboost.nosql.MapperModifier;
import javassist.ClassPool;
import javassist.CtClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public class NosqlConfiguration implements ImportBeanDefinitionRegistrar {

    private static Logger logger = LoggerFactory.getLogger(MetricInterceptor.class);
    private static boolean mapUnderscoreToCamelCase;

    static {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader
                (MapperModifier.class.getResourceAsStream("/application.properties"), StandardCharsets.UTF_8))) {
            reader.lines().filter(it -> it.startsWith("mybatis.configuration.map-underscore-to-camel-case") ||
                    it.startsWith("mybatis.configuration.mapUnderscoreToCamelCase")).findFirst()
                    .ifPresent(it -> mapUnderscoreToCamelCase = Boolean.parseBoolean(it.split("=")[1].trim()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static {
        try {
            CtClass ctClass = ClassPool.getDefault().get("org.mybatis.spring.mapper.ClassPathMapperScanner");
            ctClass.getDeclaredMethod("checkCandidate")
                    .insertAfter("if ($_) cn.mybatisboost.nosql.MapperModifier.modify($2.getBeanClassName(), " +
                            mapUnderscoreToCamelCase + ");");
            ctClass.toClass(NosqlConfiguration.class.getClassLoader(), NosqlConfiguration.class.getProtectionDomain());
        } catch (Exception e) {
            logger.error("Exception happened when configuring mybatis-boost-nosql", e);
        }
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
    }
}
