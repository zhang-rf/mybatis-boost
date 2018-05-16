package cn.mybatisboost.test;

import cn.mybatisboost.core.mapper.CrudMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProxyDogMapper extends CrudMapper<ProxyDog> {
}
