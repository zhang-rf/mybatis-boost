package cn.mybatisboost.test;

import cn.mybatisboost.core.GenericMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootApplication
@SpringBootTest(classes = GenericMapper.class)
public class NosqlQueryTest {

    @Autowired
    @Qualifier("projectNosqlMapper")
    private GenericMapper<Project> mapper;

    @Test
    public void test() throws Exception {
        System.out.println(mapper.getClass().getMethod("selectFirst").invoke(mapper));
    }
}
