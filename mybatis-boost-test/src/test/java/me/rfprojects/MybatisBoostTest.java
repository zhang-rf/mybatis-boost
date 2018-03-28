package me.rfprojects;

import me.rfprojects.core.Configuration;
import me.rfprojects.core.adaptor.PrefixNameAdaptor;
import me.rfprojects.limiter.LimiterInterceptor;
import me.rfprojects.mapper.MapperInterceptor;
import org.apache.ibatis.session.RowBounds;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestMapper.class, MybatisBoostTest.Config.class})
@SpringBootApplication
public class MybatisBoostTest {

    public static class Config {

        @Bean
        public MapperInterceptor mybatisInterceptor() {
            return new MapperInterceptor(Configuration.builder().setNameAdaptor(new PrefixNameAdaptor("T_")).build());
        }

        @Order(-1)
        @Bean
        public LimiterInterceptor limiterInterceptor() {
            return new LimiterInterceptor();
        }
    }

    @Autowired
    private TestMapper mapper;

    @Test
    public void myTest() {
        System.out.println(mapper.findAllWithRowBounds(new RowBounds(1, 1)));
    }
}
