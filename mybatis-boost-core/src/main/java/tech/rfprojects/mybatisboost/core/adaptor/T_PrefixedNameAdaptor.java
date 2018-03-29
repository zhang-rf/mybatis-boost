package tech.rfprojects.mybatisboost.core.adaptor;

public class T_PrefixedNameAdaptor implements NameAdaptor {

    @Override
    public String adapt(String name) {
        return "T_" + name;
    }
}
