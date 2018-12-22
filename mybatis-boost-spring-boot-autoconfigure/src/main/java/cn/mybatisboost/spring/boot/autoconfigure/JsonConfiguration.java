package cn.mybatisboost.spring.boot.autoconfigure;

import cn.mybatisboost.json.JsonResultSetsHandler;
import cn.mybatisboost.json.JsonTypeHandler;
import org.apache.ibatis.session.Configuration;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@ConditionalOnClass(ConfigurationCustomizer.class)
@ConditionalOnProperty(name = "mybatisboost.json.enabled", matchIfMissing = true)
public class JsonConfiguration implements ConfigurationCustomizer {

    @Bean
    public JsonResultSetsHandler jsonInterceptor() {
        return new JsonResultSetsHandler();
    }

    @Override
    public void customize(Configuration configuration) {
        configuration.getTypeHandlerRegistry().register(JsonTypeHandler.class);
    }
}
