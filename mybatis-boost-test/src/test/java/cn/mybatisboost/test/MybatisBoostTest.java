package cn.mybatisboost.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootApplication
@SpringBootTest(classes = ProjectMapper.class)
public class MybatisBoostTest {

    @Autowired
    private ProjectMapper mapper;

    @Test
    public void myTest() {
        Project project = new Project("cn.mybatisboost", "mybatis-boost", "MIT",
                "https://github.com/zhang-rf/mybatis-boost", "zhangrongfan");
        System.out.println(mapper.insertSelectively(project));
        System.out.println(project.getId());
    }
}
