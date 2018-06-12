package cn.mybatisboost.test;

import cn.mybatisboost.core.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface ProxyDogMapper extends CrudMapper<ProxyDog> {

    @Select("select now()")
    LocalDateTime now();
}
