package cn.mybatisboost.spring.boot.autoconfigure;

import javassist.ClassPool;
import javassist.CtClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

@ConditionalOnProperty(name = "mybatisboost.nosql.enabled", matchIfMissing = true)
public class NosqlConfiguration implements ImportBeanDefinitionRegistrar {

    private static Logger logger = LoggerFactory.getLogger(NosqlConfiguration.class);
    private static boolean mapUnderscoreToCamelCase;

    static {
        try {
            mapUnderscoreToCamelCase = EnvironmentHolder.getEnvironment()
                    .getProperty("mybatis.configuration.map-underscore-to-camel-case",
                            boolean.class, false);
        } catch (EnvironmentHolder.EnvironmentNotSetException e) {
            logger.warn("Use default mapUnderscoreToCamelCase value, because Environment wasn't set");
        }
        try {
            CtClass ctClass = ClassPool.getDefault().get("org.mybatis.spring.mapper.ClassPathMapperScanner");
            ctClass.getDeclaredMethod("checkCandidate").insertAfter("if ($_) " +
                    "cn.mybatisboost.nosql.MapperInstrument.modify" +
                    "($2.getBeanClassName(), " + mapUnderscoreToCamelCase + ");");
            ctClass.toClass(NosqlConfiguration.class.getClassLoader(),
                    NosqlConfiguration.class.getProtectionDomain());
        } catch (Exception e) {
            logger.error("Exception happened when configuring mybatisboost.nosql", e);
        }
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // Noop
    }
}
