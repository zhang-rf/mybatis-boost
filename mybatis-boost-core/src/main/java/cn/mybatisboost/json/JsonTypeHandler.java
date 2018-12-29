package cn.mybatisboost.json;

import cn.mybatisboost.support.JsonType;
import cn.mybatisboost.util.PropertyUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@MappedTypes({JsonType.class, List.class, Map.class})
public class JsonTypeHandler extends BaseTypeHandler<Object> {

    private static Logger logger = LoggerFactory.getLogger(JsonTypeHandler.class);
    static ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
    static ThreadLocal<List<String>> tlProperties = ThreadLocal.withInitial(ArrayList::new);
    static ThreadLocal<List<String>> tlResults = ThreadLocal.withInitial(ArrayList::new);

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType)
            throws SQLException {
        try {
            ps.setString(i, objectMapper.writeValueAsString(parameter));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        List<String> resultNames = tlProperties.get();
        String property = PropertyUtils.normalizeProperty(columnName);
        if (!resultNames.contains(property)) {
            resultNames.add(property);
        }
        tlResults.get().add(rs.getString(columnName));
        return null;
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) {
        logger.warn("Json type is not supported when using CallableStatement");
        return null;
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) {
        logger.warn("Json type is not supported when using CallableStatement");
        return null;
    }
}
