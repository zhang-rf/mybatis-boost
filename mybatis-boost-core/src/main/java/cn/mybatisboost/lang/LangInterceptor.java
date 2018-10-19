package cn.mybatisboost.lang;

import cn.mybatisboost.core.Configuration;
import cn.mybatisboost.core.ConfigurationAware;
import cn.mybatisboost.core.SqlProvider;
import cn.mybatisboost.core.util.MyBatisUtils;
import cn.mybatisboost.lang.provider.*;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.reflection.MetaObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class LangInterceptor implements Interceptor {

    private Configuration configuration;
    private List<SqlProvider> providers;

    public LangInterceptor(Configuration configuration) {
        this.configuration = configuration;
        initProviders();
    }

    protected void initProviders() {
        providers = Collections.unmodifiableList(Arrays.asList(new InsertEnhancement(), new UpdateEnhancement(),
                new TableEnhancement(), new ListParameterEnhancement(), new ParameterMappingEnhancement(),
                new NullEnhancement()));
        for (SqlProvider provider : providers) {
            if (provider instanceof ConfigurationAware) {
                ((ConfigurationAware) provider).setConfiguration(configuration);
            }
        }
    }

    @Override
    public Object intercept(Invocation invocation) {
        MetaObject metaObject = MyBatisUtils.getRealMetaObject(invocation.getTarget());
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
        providers.forEach(p -> p.replace(metaObject, mappedStatement, boundSql));
        return null;
    }

    @Override
    public Object plugin(Object target) {
        return target;
    }

    @Override
    public void setProperties(Properties properties) {
    }
}
