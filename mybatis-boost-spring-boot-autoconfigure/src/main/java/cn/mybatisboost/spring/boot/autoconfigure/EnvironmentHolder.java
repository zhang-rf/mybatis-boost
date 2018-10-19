package cn.mybatisboost.spring.boot.autoconfigure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

public class EnvironmentHolder implements EnvironmentPostProcessor {

    private static ConfigurableEnvironment environment;

    public static ConfigurableEnvironment getEnvironment() {
        if (environment == null) {
            throw new Error("Environment haven't been set yet");
        }
        return environment;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        EnvironmentHolder.environment = environment;
    }
}
