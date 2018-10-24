package cn.mybatisboost.test;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootApplication
@SpringBootTest(classes = ProjectMapper.class)
public class EnhancementTest {

    @Autowired
    private ProjectMapper mapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @After
    public void tearDown() {
        jdbcTemplate.execute("delete from project");
    }

    @Test
    public void selectFromT() {
        assertTrue(mapper.selectFromT().isEmpty());
        jdbcTemplate.execute("insert into project (group_id) values ('cn.mybatisboost1')");
        jdbcTemplate.execute("insert into project (group_id) values ('cn.mybatisboost2')");
        assertEquals(2, mapper.selectFromT().size());
    }

    @Test
    public void selectRange() {
        assertTrue(mapper.selectRange(Arrays.asList(123, 456)).isEmpty());
        jdbcTemplate.execute("insert into project (id, group_id) values (123, 'cn.mybatisboost1')");
        jdbcTemplate.execute("insert into project (id, group_id) values (456, 'cn.mybatisboost2')");
        assertEquals(2, mapper.selectRange(Arrays.asList(123, 456)).size());
        assertEquals(123, (int) mapper.selectRange(Collections.singletonList(123)).get(0).getId());
        assertEquals(456, (int) mapper.selectRange(Collections.singletonList(456)).get(0).getId());
        assertEquals(2, mapper.selectRange2(Arrays.asList(123, 456)).size());
    }

    @Test
    public void selectOneFromT() {
        assertNull(mapper.selectOneFromT(123));
        jdbcTemplate.execute("insert into project (id, group_id) values (123, 'cn.mybatisboost1')");
        jdbcTemplate.execute("insert into project (id, group_id) values (456, 'cn.mybatisboost2')");
        assertEquals(123, (int) mapper.selectOneFromT(123).getId());
    }

    @Test
    public void insertSome() {
        assertEquals(1, mapper.insertSome(Collections.singletonList(new Project(
                "cn.mybatisboost", "mybatis-boost",
                "MIT", "https://github.com/zhang-rf/mybatis-boost", "zhangrongfan"))));
        jdbcTemplate.query("select * from project", resultSet -> {
            assertEquals(1, resultSet.getRow());
            assertEquals("cn.mybatisboost", resultSet.getString("group_id"));
            assertEquals("mybatis-boost", resultSet.getString("artifact_id"));
        });
    }

    @Test
    public void insertOne1() {
        assertEquals(1, mapper.insertOne1(new Project("cn.mybatisboost", "mybatis-boost",
                "MIT", "https://github.com/zhang-rf/mybatis-boost", "zhangrongfan")));
        jdbcTemplate.query("select * from project", resultSet -> {
            assertEquals(1, resultSet.getRow());
            assertEquals("cn.mybatisboost", resultSet.getString("group_id"));
            assertNull(resultSet.getString("artifact_id"));
        });
    }

    @Test
    public void insertOne2() {
        assertEquals(1, mapper.insertOne2(new Project("cn.mybatisboost", "mybatis-boost",
                "MIT", "https://github.com/zhang-rf/mybatis-boost", "zhangrongfan")));
        jdbcTemplate.query("select * from project", resultSet -> {
            assertEquals(1, resultSet.getRow());
            assertEquals("cn.mybatisboost", resultSet.getString("group_id"));
            assertNull(resultSet.getString("artifact_id"));
        });
    }

    @Test
    public void updateGroupId() {
        jdbcTemplate.execute("insert into project (id, group_id) values (123, 'cn.mybatisboost1')");
        assertEquals(1, mapper.updateGroupId("cn.mybatisboost2", 123));
        jdbcTemplate.query("select * from project", resultSet -> {
            assertEquals(1, resultSet.getRow());
            assertEquals("cn.mybatisboost2", resultSet.getString("group_id"));
            assertNull(resultSet.getString("artifact_id"));
        });
    }

    @Test
    public void updateNotGroupId() {
        jdbcTemplate.execute("insert into project (group_id) values ('cn.mybatisboost1')");
        assertEquals(1, mapper.updateNotGroupId(new Project("cn.mybatisboost", "mybatis-boost",
                "MIT", "https://github.com/zhang-rf/mybatis-boost", "zhangrongfan")));
        jdbcTemplate.query("select * from project", resultSet -> {
            assertEquals(1, resultSet.getRow());
            assertEquals("cn.mybatisboost1", resultSet.getString("group_id"));
            assertEquals("mybatis-boost", resultSet.getString("artifact_id"));
            assertEquals("MIT", resultSet.getString("license"));
            assertEquals("https://github.com/zhang-rf/mybatis-boost", resultSet.getString("scm"));
            assertEquals("zhangrongfan", resultSet.getString("developer"));
        });
    }

    @Test
    public void updateAll() {
        jdbcTemplate.execute("insert into project (group_id) values ('cn.mybatisboost1')");
        assertEquals(1, mapper.updateAll(new Project("cn.mybatisboost", "mybatis-boost",
                "MIT", "https://github.com/zhang-rf/mybatis-boost", "zhangrongfan")));
        jdbcTemplate.query("select * from project", resultSet -> {
            assertEquals(1, resultSet.getRow());
            assertEquals("cn.mybatisboost", resultSet.getString("group_id"));
            assertEquals("mybatis-boost", resultSet.getString("artifact_id"));
            assertEquals("MIT", resultSet.getString("license"));
            assertEquals("https://github.com/zhang-rf/mybatis-boost", resultSet.getString("scm"));
            assertEquals("zhangrongfan", resultSet.getString("developer"));
        });
    }
}