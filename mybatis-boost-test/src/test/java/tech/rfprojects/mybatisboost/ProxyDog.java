package tech.rfprojects.mybatisboost;

import javax.persistence.Id;

public class ProxyDog {

    @Id
    private long id;
    private String netAddress;

    public ProxyDog() {
    }

    public ProxyDog(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNetAddress() {
        return netAddress;
    }

    public void setNetAddress(String netAddress) {
        this.netAddress = netAddress;
    }
}
