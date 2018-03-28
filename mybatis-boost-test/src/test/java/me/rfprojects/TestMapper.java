package me.rfprojects;

import me.rfprojects.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestMapper extends GenericMapper<ProxyDog> {
}
