package cn.mybatisboost.test;

import cn.mybatisboost.support.Bean;

public class Website extends Bean {

    private String protocol;
    private String host;
    private short port;

    public Website() {
    }

    public Website(String protocol, String host, short port) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public short getPort() {
        return port;
    }

    public void setPort(short port) {
        this.port = port;
    }
}
