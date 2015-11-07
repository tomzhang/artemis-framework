package com.spinn3r.artemis.http.init;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebserverConfig {

    private int port;

    private int maxThreads = 500;

    public WebserverConfig() {}

    public WebserverConfig(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    @Override
    public String toString() {
        return "WebserverConfig{" +
                 "port=" + port +
                 ", maxThreads=" + maxThreads +
                 '}';
    }

}