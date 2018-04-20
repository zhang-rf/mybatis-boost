package cn.mybatisboost.test;

public class ProxyDog {

    private Long id;
    private String dogId;
    private String netAddress;
    private int useCount;

    public ProxyDog() {
    }

    public ProxyDog(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public ProxyDog setId(Long id) {
        this.id = id;
        return this;
    }

    public String getDogId() {
        return dogId;
    }

    public ProxyDog setDogId(String dogId) {
        this.dogId = dogId;
        return this;
    }

    public String getNetAddress() {
        return netAddress;
    }

    public ProxyDog setNetAddress(String netAddress) {
        this.netAddress = netAddress;
        return this;
    }

    public int getUseCount() {
        return useCount;
    }

    public ProxyDog setUseCount(int useCount) {
        this.useCount = useCount;
        return this;
    }

    @Override
    public String toString() {
        return "ProxyDog{" +
                "id=" + id +
                ", dogId='" + dogId + '\'' +
                ", netAddress='" + netAddress + '\'' +
                ", useCount=" + useCount +
                '}';
    }
}
