package cn.mybatisboost.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootApplication
@SpringBootTest(classes = ProjectMapper.class)
public class CrudMapperTest {

    @Autowired
    private ProjectMapper mapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void count() {
        assertEquals(0, mapper.count(new Project()));
        jdbcTemplate.execute("insert into project (groud_id) values ('cn.mybatisboost')");
        assertEquals(1, mapper.count(new Project()));
        assertEquals(1, mapper.count(new Project().setGroupId("cn.mybatisboost")));
        assertEquals(0, mapper.count(new Project().setGroupId("group id")));
    }

    @Test
    public void selectOne() {
    }

    @Test
    public void select() {
    }

    @Test
    public void selectWithRowBounds() {
    }

    @Test
    public void countAll() {
    }

    @Test
    public void selectAll() {
    }

    @Test
    public void selectAllWithRowBounds() {
    }

    @Test
    public void selectById() {
    }

    @Test
    public void selectByIds() {
    }

    @Test
    public void insert() {
    }

    @Test
    public void batchInsert() {
    }

    @Test
    public void insertSelectively() {
    }

    @Test
    public void batchInsertSelectively() {
    }

    @Test
    public void update() {
    }

    @Test
    public void updatePartially() {
    }

    @Test
    public void updateSelectively() {
    }

    @Test
    public void updatePartiallySelectively() {
    }

    @Test
    public void delete() {
    }

    @Test
    public void deleteByIds() {
    }
}