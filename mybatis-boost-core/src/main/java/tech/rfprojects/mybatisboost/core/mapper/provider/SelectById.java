package tech.rfprojects.mybatisboost.core.mapper.provider;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.MetaObject;
import tech.rfprojects.mybatisboost.core.Configuration;
import tech.rfprojects.mybatisboost.core.ConfigurationAware;
import tech.rfprojects.mybatisboost.core.SqlProvider;
import tech.rfprojects.mybatisboost.core.util.EntityUtils;
import tech.rfprojects.mybatisboost.core.util.MyBatisUtils;
import tech.rfprojects.mybatisboost.core.util.SqlUtils;

import java.util.*;

public class SelectById implements SqlProvider, ConfigurationAware {

    private Configuration configuration;

    @Override
    public void replace(MetaObject metaObject, MappedStatement mappedStatement, BoundSql boundSql) {
        Class<?> type = mappedStatement.getResultMaps().get(0).getType();

        String tableName = EntityUtils.getTableName(type, configuration.getNameAdaptor());
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT * FROM ").append(tableName);

        List<String> properties = EntityUtils.getProperties(type);
        boolean mapUnderscoreToCamelCase = (boolean)
                metaObject.getValue("delegate.configuration.mapUnderscoreToCamelCase");
        List<String> columns = EntityUtils.getColumns(type, properties, mapUnderscoreToCamelCase);

        List<Integer> idIndexes = Collections.singletonList(EntityUtils.getIdIndex(type, properties));
        List<String> ids = new ArrayList<>();
        idIndexes.forEach(i -> ids.add(columns.get(i)));

        SqlUtils.appendWhere(sqlBuilder, ids);
        List<ParameterMapping> parameterMappings = MyBatisUtils.getParameterMapping
                ((org.apache.ibatis.session.Configuration)
                        metaObject.getValue("delegate.configuration"), ids);
        metaObject.setValue("delegate.boundSql.parameterMappings", parameterMappings);

        Map parameterMap = (Map) boundSql.getParameterObject();
        Object[] parameterArray = (Object[]) parameterMap.get("array");
        parameterMap = new HashMap<>();
        for (int i = 0; i < ids.size(); i++) {
            //noinspection unchecked
            parameterMap.put(ids.get(0), parameterArray[i]);
        }
        metaObject.setValue("delegate.boundSql.parameterObject", parameterMap);
        metaObject.setValue("delegate.parameterHandler.parameterObject", parameterMap);

        metaObject.setValue("delegate.boundSql.sql", sqlBuilder.toString());
    }

    @Override
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
