package cn.mybatisboost.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootApplication
@SpringBootTest(classes = ProjectMapper.class)
public class JsonTest {

    @Autowired
    private ProjectMapper mapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @After
    public void tearDown() {
        jdbcTemplate.execute("delete from project");
    }

    @Test
    public void testSave() {
        Project project = new Project("cn.mybatisboost", "mybatis-boost",
                "MIT", "https://github.com/zhang-rf/mybatis-boost", "zhangrongfan",
                new Website("HTTPS", "mybatisboost.cn", (short) 80));
        mapper.insert(project);
        jdbcTemplate.query("select * from project", resultSet -> {
            try {
                assertEquals(objectMapper.writeValueAsString(project.getWebsite()), resultSet.getString("website"));
            } catch (JsonProcessingException e) {
                fail();
            }
        });
    }

    @Test
    public void testQuery() throws Exception {
        Project project = new Project("cn.mybatisboost", "mybatis-boost",
                "MIT", "https://github.com/zhang-rf/mybatis-boost", "zhangrongfan",
                new Website("HTTPS", "mybatisboost.cn", (short) 80));
        jdbcTemplate.execute("insert into project (id, group_id, website) values (999, 'cn.mybatisboost', '" + objectMapper.writeValueAsString(project.getWebsite()) + "')");
        project = mapper.selectById(999);
        assertNotNull(project);
        assertEquals("HTTPS", project.getWebsite().getProtocol());
        assertEquals("mybatisboost.cn", project.getWebsite().getHost());
        assertEquals(80, project.getWebsite().getPort());
    }
}
