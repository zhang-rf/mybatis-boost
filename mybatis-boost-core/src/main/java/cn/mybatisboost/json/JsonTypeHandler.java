package cn.mybatisboost.json;

import cn.mybatisboost.support.Property;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@MappedTypes(Property.class)
public class JsonTypeHandler extends BaseTypeHandler<Property<?>> {

    static ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Property<?> parameter, JdbcType jdbcType)
            throws SQLException {
        try {
            ps.setString(i, objectMapper.writeValueAsString(parameter.get()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Property<?> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return Property.ofNullable(rs.getString(columnName));
    }

    @Override
    public Property<?> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return Property.ofNullable(rs.getString(columnIndex));
    }

    @Override
    public Property<?> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return Property.ofNullable(cs.getString(columnIndex));
    }
}
