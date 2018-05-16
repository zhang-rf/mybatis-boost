package cn.mybatisboost.test;

import cn.mybatisboost.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProxyDogMapper extends BaseMapper<ProxyDog> {
}
