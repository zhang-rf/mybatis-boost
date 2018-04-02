package tech.rfprojects.mybatisboost.core.adaptor;

public class TPrefixedNameAdaptor implements NameAdaptor {

    @Override
    public String adapt(String name) {
        return "T_" + name;
    }
}
