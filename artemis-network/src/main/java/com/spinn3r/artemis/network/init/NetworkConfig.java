package com.spinn3r.artemis.network.init;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import com.spinn3r.artemis.network.ResourceRequestFactory;
import com.spinn3r.artemis.network.URLResourceRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NetworkConfig {

    private String userAgent;

    private boolean blockSSL;

    private String defaultProxy;

    private Map<String,Proxy> proxies = new HashMap<>();

    private int defaultMaxContentLength = URLResourceRequest.MAX_CONTENT_LENGTH;

    private int defaultReadTimeout = ResourceRequestFactory.DEFAULT_READ_TIMEOUT;

    private int defaultConnectTimeout = ResourceRequestFactory.DEFAULT_CONNECT_TIMEOUT;

    private boolean requireProxy = false;

    private List<Request> requests = Lists.newArrayList();

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public boolean getBlockSSL() {
        return blockSSL;
    }

    public void setBlockSSL(boolean blockSSL) {
        this.blockSSL = blockSSL;
    }

    public String getDefaultProxy() {
        return defaultProxy;
    }

    public void setDefaultProxy(String defaultProxy) {
        this.defaultProxy = defaultProxy;
    }

    public Map<String, Proxy> getProxies() {
        return proxies;
    }

    public int getDefaultMaxContentLength() {
        return defaultMaxContentLength;
    }

    public void setDefaultMaxContentLength(int defaultMaxContentLength) {
        this.defaultMaxContentLength = defaultMaxContentLength;
    }

    public int getDefaultReadTimeout() {
        return defaultReadTimeout;
    }

    public void setDefaultReadTimeout(int defaultReadTimeout) {
        this.defaultReadTimeout = defaultReadTimeout;
    }

    public int getDefaultConnectTimeout() {
        return defaultConnectTimeout;
    }

    public void setDefaultConnectTimeout(int defaultConnectTimeout) {
        this.defaultConnectTimeout = defaultConnectTimeout;
    }

    public boolean getRequireProxy() {
        return requireProxy;
    }

    public void setRequireProxy(boolean requireProxy) {
        this.requireProxy = requireProxy;
    }

    public List<Request> getRequests() {
        return requests;
    }

    @Override
    public String toString() {
        return "NetworkConfig{" +
                 "userAgent='" + userAgent + '\'' +
                 ", blockSSL=" + blockSSL +
                 ", defaultProxy='" + defaultProxy + '\'' +
                 ", proxies=" + proxies +
                 ", defaultMaxContentLength=" + defaultMaxContentLength +
                 ", defaultReadTimeout=" + defaultReadTimeout +
                 ", defaultConnectTimeout=" + defaultConnectTimeout +
                 ", requireProxy=" + requireProxy +
                 ", requests=" + requests +
                 '}';
    }

    /**
     * Configurations for HTTP requests defined by regex.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Request {

        private String regex;

        private int priority;

        private String userAgent = null;

        private Map<String,String> cookies = null;

        private Map<String,String> requestHeaders = null;

        private Long connectTimeout = null;

        private Long readTimeout = null;

        private Boolean followRedirects = null;

        private Boolean followContentRedirects = null;

        public String getRegex() {
            return regex;
        }

        public int getPriority() {
            return priority;
        }

        public String getUserAgent() {
            return userAgent;
        }

        public Map<String, String> getCookies() {
            return cookies;
        }

        public Map<String, String> getRequestHeaders() {
            return requestHeaders;
        }

        public Long getConnectTimeout() {
            return connectTimeout;
        }

        public Long getReadTimeout() {
            return readTimeout;
        }

        public Boolean getFollowRedirects() {
            return followRedirects;
        }

        public Boolean getFollowContentRedirects() {
            return followContentRedirects;
        }

        @Override
        public String toString() {
            return "Request{" +
                     "regex='" + regex + '\'' +
                     ", userAgent='" + userAgent + '\'' +
                     ", cookies=" + cookies +
                     ", requestHeaders=" + requestHeaders +
                     ", connectTimeout=" + connectTimeout +
                     ", readTimeout=" + readTimeout +
                     ", followRedirects=" + followRedirects +
                     ", followContentRedirects=" + followContentRedirects +
                     '}';
        }

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Proxy {

        public Proxy() {
        }

        private String host;

        private int port;

        private String regex;

        private int priority;

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getRegex() {
            return regex;
        }

        public int getPriority() {
            return priority;
        }

        @Override
        public String toString() {
            return "Proxy{" +
                     "host='" + host + '\'' +
                     ", port=" + port +
                     ", regex='" + regex + '\'' +
                     ", priority=" + priority +
                     '}';
        }

    }

}
