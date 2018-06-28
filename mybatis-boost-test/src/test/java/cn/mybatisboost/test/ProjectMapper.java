package cn.mybatisboost.test;

import cn.mybatisboost.core.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface ProjectMapper extends CrudMapper<Project> {

    @Select("select now()")
    LocalDateTime now();
}
