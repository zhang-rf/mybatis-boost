package cn.mybatisboost.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootApplication
@SpringBootTest(classes = ProxyDogMapper.class)
public class MybatisBoostTest {

    @Autowired
    private ProxyDogMapper mapper;

    @Test
    public void myTest() {
        System.out.println(mapper.selectAll());
    }
}
