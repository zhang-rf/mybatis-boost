package cn.mybatisboost.spring.boot.autoconfigure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

public class EnvironmentHolder implements EnvironmentPostProcessor {

    private static volatile ConfigurableEnvironment environment;

    public static ConfigurableEnvironment getEnvironment() {
        if (environment == null) {
            throw new EnvironmentNotSetException();
        }
        return environment;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        EnvironmentHolder.environment = environment;
    }

    public static class EnvironmentNotSetException extends RuntimeException {
    }
}
