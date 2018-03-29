package tech.rfprojects.mybatisboost;

import org.apache.ibatis.annotations.Mapper;
import tech.rfprojects.mybatisboost.mapper.GenericMapper;

@Mapper
public interface TestMapper extends GenericMapper<ProxyDog> {
}
