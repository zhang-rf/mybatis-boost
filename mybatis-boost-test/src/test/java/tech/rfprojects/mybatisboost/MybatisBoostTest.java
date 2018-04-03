package tech.rfprojects.mybatisboost;

import org.apache.ibatis.session.RowBounds;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootApplication
@SpringBootTest(classes = TestMapper.class)
public class MybatisBoostTest {

    @Autowired
    private TestMapper mapper;

    @Test
    public void myTest() {
        System.out.println(mapper.selectAllWithRowBounds(new RowBounds(1, 1)));
    }
}
