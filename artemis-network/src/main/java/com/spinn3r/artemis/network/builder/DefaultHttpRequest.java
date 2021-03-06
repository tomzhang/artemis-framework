package com.spinn3r.artemis.network.builder;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.spinn3r.artemis.network.NetworkException;
import com.spinn3r.artemis.network.ResourceRequest;
import com.spinn3r.artemis.network.URLResourceRequest;
import com.spinn3r.artemis.network.builder.listener.RequestListener;
import com.spinn3r.artemis.network.cookies.Cookie;
import com.spinn3r.artemis.network.cookies.Cookies;

/**
 *
 */
public class DefaultHttpRequest implements HttpRequest {

    private final DefaultHttpRequestBuilder defaultHttpRequestBuilder;

    private final DefaultHttpRequestMethod defaultHttpRequestMethod;

    private final ResourceRequest resourceRequest;

    private String contentWithEncoding = null;

    private ImmutableList<Cookie> effectiveCookies = ImmutableList.of();

    private boolean executed = false;

    public DefaultHttpRequest( DefaultHttpRequestBuilder defaultHttpRequestBuilder,
                               DefaultHttpRequestMethod defaultHttpRequestMethod,
                               ResourceRequest resourceRequest) {

        this.defaultHttpRequestBuilder = defaultHttpRequestBuilder;
        this.defaultHttpRequestMethod = defaultHttpRequestMethod;
        this.resourceRequest = resourceRequest;

    }

    /**
     * Get the content of this URL with charset encoding applied.
     *
     * Note that this method is lazy and only decodes the content on the first
     * call and then returns the previously encoded content with each additional
     * call.
     */
    @Override
    public String getContentWithEncoding() throws NetworkException {

        if ( contentWithEncoding == null ) {

            contentWithEncoding = resourceRequest.getContentWithEncoding();

            if (defaultHttpRequestBuilder.requestListeners != null) {

                for (RequestListener requestListener : defaultHttpRequestBuilder.requestListeners) {
                    requestListener.onContentWithEncoding( getHttpRequestMeta(), contentWithEncoding );
                }

            }

        }

        onExecuted();

        return contentWithEncoding;

    }

    @Override
    public HttpRequest connect() throws NetworkException {
        resourceRequest.connect();
        onExecuted();

        return this;
    }

    // called internally once this request has been executed.
    private void onExecuted() {

        if ( executed ) {
            // only allow this to be called once... otherwise we could
            // call getContentWithEncoding twice and overwrite cookies.
            return;
        }

        this.effectiveCookies = ImmutableList.copyOf(defaultHttpRequestBuilder.threadLocalCookies.getCookies());
        defaultHttpRequestBuilder.threadLocalCookies.flush();
        executed = true;
    }

    @Override
    public void disconnect() throws NetworkException {
        resourceRequest.disconnect();
    }

    @Override
    public InputStream getInputStream() throws NetworkException {

        try {

            return resourceRequest.getInputStream();

        } catch (IOException e) {
            throw new NetworkException( e );
        }

    }

    @Override
    public InputStream getDirectInputStream() throws NetworkException {
        return ((URLResourceRequest)resourceRequest).getBackendInputStream();
    }

    @Override
    public int getResponseCode() {
        return resourceRequest.getResponseCode();
    }


    @Override
    public String getRequestHeader(String name) {
        return resourceRequest.getRequestHeader( name );
    }

    @Override
    public Set<String> getRequestHeaderNames() {
        return resourceRequest.getRequestHeaders();
    }

    @Override
    public ImmutableMap<String,String> getRequestHeadersMap() {
        return ImmutableMap.copyOf( resourceRequest.getRequestHeadersMap() );
    }

    @Override
    public ImmutableList<Cookie> getCookies() {
        return ImmutableList.copyOf( defaultHttpRequestMethod.cookies );
    }

    @Override
    public String getResponseHeader(String name) {
        return resourceRequest.getResponseHeader( name );
    }

    @Override
    public Set<String> getResponseHeaderNames() {
        return resourceRequest.getResponseHeaders();
    }

    @Override
    public ImmutableMap<String,ImmutableList<String>> getResponseHeadersMap() {

        Map<String, Collection<String>> responseHeadersMap = resourceRequest.getResponseHeadersMap();

        // TODO: move to use Immutables.copyMultiMap

        Map<String,ImmutableList<String>> tmp = Maps.newHashMap();

        for (Map.Entry<String, Collection<String>> entry : responseHeadersMap.entrySet()) {

            if ( entry.getKey() == null ) {
                // the HTTP status line is included here as a null key which is
                // really ugly and we should hide it.  It also yields bugs
                // for example Jackson refuses to encode it.
                continue;
            }

            tmp.put( entry.getKey(), ImmutableList.copyOf( entry.getValue() ) );
        }

        return ImmutableMap.copyOf( tmp );

    }

    @Override
    public String getResource() {
        return resourceRequest.getResource();
    }

    @Override
    public String getResourceFromRedirect() {
        return resourceRequest.getResourceFromRedirect();
    }

    @Override
    public long getDuration() {
        return resourceRequest.getDuration();
    }

    @Override
    public InetAddress getInetAddress() {
        return resourceRequest.getInetAddress();
    }

    @Override
    public int getContentLength() {
        return resourceRequest.getContentLength();
    }

    @Override
    public HttpRequestMethod getMethod() {
        return defaultHttpRequestMethod;
    }

    @Override
    public NetworkException getCause() {
        return resourceRequest.getCause();
    }

    @Override
    public void setCause( NetworkException cause ) {
        resourceRequest.setCause( cause );
    }

    @Override
    public Class<?> getExecutor() {
        return defaultHttpRequestMethod.executor;
    }

    @Override
    public HttpRequestMeta getHttpRequestMeta() {

        return new DefaultHttpRequestMeta( getResource(),
                                           getRequestHeadersMap(),
                                           Cookies.toMap( defaultHttpRequestMethod.cookies ),
                                           defaultHttpRequestMethod.outputContent,
                                           defaultHttpRequestMethod.outputContentEncoding,
                                           defaultHttpRequestMethod.outputContentType,
                                           defaultHttpRequestBuilder.getUserAgent());

    }

    @Override
    public HttpResponseMeta getHttpResponseMeta() throws NetworkException {

        connect();

        return new DefaultHttpResponseMeta(getResource(), getResourceFromRedirect(), getResponseCode(), getResponseHeadersMap(), Cookies.toCookieMap(getEffectiveCookies()) );

    }

    @Override
    public HttpContentResponseMeta getHttpContentResponseMeta() throws NetworkException {

        String contentWithEncoding = getContentWithEncoding();

        return new DefaultHttpContentResponseMeta( getResource(), getResourceFromRedirect(), getResponseCode(), getResponseHeadersMap(), Cookies.toCookieMap(getEffectiveCookies()), contentWithEncoding );

    }

    @Override
    public ImmutableList<Cookie> getEffectiveCookies() {
        return effectiveCookies;
    }
}

