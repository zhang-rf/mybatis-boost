package tech.rfprojects.mybatisboost.core.adaptor;

public class NoopNameAdaptor implements NameAdaptor {

    @Override
    public String adapt(String name) {
        return name;
    }
}
