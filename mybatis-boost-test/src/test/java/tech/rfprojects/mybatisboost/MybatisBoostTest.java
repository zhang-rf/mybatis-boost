package tech.rfprojects.mybatisboost;

import org.apache.ibatis.session.RowBounds;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import tech.rfprojects.mybatisboost.core.Configuration;
import tech.rfprojects.mybatisboost.core.adaptor.PrefixNameAdaptor;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {TestMapper.class, MybatisBoostTest.Config.class})
@SpringBootApplication
public class MybatisBoostTest {

    public static class Config {

        @Bean
        public Configuration configuration() {
            return Configuration.builder().setNameAdaptor(new PrefixNameAdaptor("T_")).build();
        }
    }

    @Autowired
    private TestMapper mapper;

    @Test
    public void myTest() {
        System.out.println(mapper.findAllWithRowBounds(new RowBounds(1, 1)));
    }
}
