package cn.mybatisboost.test;

import org.apache.ibatis.annotations.Mapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootApplication(scanBasePackageClasses = Mapper.class)
@SpringBootTest
public class NosqlQueryTest {

    @Autowired
    private ProjectMapper mapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        jdbcTemplate.execute("insert into project (id, group_id, artifact_id) values (123, 'cn.mybatisboost1', 'mybatis-boost1')");
        jdbcTemplate.execute("insert into project (id, group_id, artifact_id) values (456, 'cn.mybatisboost2', 'mybatis-boost2')");
        jdbcTemplate.execute("insert into project (id, group_id, artifact_id) values (789, 'cn.mybatisboost3', 'mybatis-boost3')");
    }

    @After
    public void tearDown() {
        jdbcTemplate.execute("delete from project");
    }

    @Test
    public void deleteAll() {
        assertEquals(3, mapper.deleteAll());
        jdbcTemplate.query("select * from project", resultSet -> {
            assertEquals(0, resultSet.getRow());
        });
    }

    @Test
    public void selectFirst() {
        assertEquals(123, (int) mapper.selectFirst().getId());
    }

    @Test
    public void selectTop2() {
        List<Project> list = mapper.selectTop2();
        assertEquals(2, list.size());
        assertEquals(123, (int) list.get(0).getId());
        assertEquals(456, (int) list.get(1).getId());
    }

    @Test
    public void selectAllOffset1Limit1() {
        assertEquals(456, (int) mapper.selectAllOffset1Limit1().getId());
    }

    @Test
    public void selectByGroupIdAndArtifactId() {
        assertEquals(123, (int) mapper.selectByGroupIdAndArtifactId("cn.mybatisboost1", "mybatis-boost1").get(0).getId());
    }

    @Test
    public void selectByGroupIdOrArtifactId() {
        List<Project> list = mapper.selectByGroupIdOrArtifactId("cn.mybatisboost1", "mybatis-boost2");
        assertEquals(2, list.size());
        assertEquals(123, (int) list.get(0).getId());
        assertEquals(456, (int) list.get(1).getId());
    }

    @Test
    public void selectByNotArtifactId() {
        List<Project> list = mapper.selectByNotArtifactId("mybatis-boost1");
        assertEquals(2, list.size());
        assertEquals(456, (int) list.get(0).getId());
        assertEquals(789, (int) list.get(1).getId());
    }

    @Test
    public void selectAllOrderByGroupIdDesc() {
        List<Project> list = mapper.selectAllOrderByGroupIdDesc();
        assertEquals(3, list.size());
        assertEquals(789, (int) list.get(0).getId());
        assertEquals(456, (int) list.get(1).getId());
        assertEquals(123, (int) list.get(2).getId());
    }
}
