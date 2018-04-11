package tech.rfprojects.mybatisboost;

import org.apache.ibatis.annotations.Mapper;
import tech.rfprojects.mybatisboost.core.mapper.GenericMapper;

@Mapper
public interface ProxyDogMapper extends GenericMapper<ProxyDog, Long> {
}
