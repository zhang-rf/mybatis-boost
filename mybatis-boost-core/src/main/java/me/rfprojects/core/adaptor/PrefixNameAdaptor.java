package me.rfprojects.core.adaptor;

public class PrefixNameAdaptor implements NameAdaptor {

    private final String prefix;

    public PrefixNameAdaptor(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String adapt(String name) {
        return prefix + name;
    }
}
