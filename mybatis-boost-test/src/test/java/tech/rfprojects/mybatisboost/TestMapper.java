package tech.rfprojects.mybatisboost;

import org.apache.ibatis.annotations.Mapper;
import tech.rfprojects.mybatisboost.core.mapper.GenericMapper;

@Mapper
public interface TestMapper extends GenericMapper<ProxyDog, Integer> {
}
