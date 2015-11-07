package com.spinn3r.artemis.network.init;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.spinn3r.artemis.init.AtomicReferenceProvider;
import com.spinn3r.artemis.network.builder.DefaultHttpRequestBuilder;
import com.spinn3r.artemis.network.builder.DirectHttpRequestBuilder;
import com.spinn3r.artemis.network.builder.config.NetworkConfigRequestRegistry;
import com.spinn3r.artemis.network.builder.listener.RequestListeners;
import com.spinn3r.artemis.network.builder.proxies.ProxyReference;
import com.spinn3r.artemis.network.builder.proxies.ProxyRegistry;
import com.spinn3r.artemis.network.fetcher.ContentFetcher;
import com.spinn3r.artemis.network.fetcher.DefaultContentFetcher;
import com.spinn3r.artemis.util.daemon.WaitForPort;
import com.spinn3r.artemis.init.BaseService;
import com.spinn3r.artemis.init.Config;
import com.spinn3r.artemis.network.builder.ConfiguredHttpRequestBuilder;
import com.spinn3r.artemis.network.builder.HttpRequestBuilder;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;

/**
 * Network service which instruments requests.
 */

@Config( path = "network.conf",
         required = true,
         implementation = NetworkConfig.class )
public class NetworkService extends BaseService {

    private static final int TIMEOUT = 60000;

    private final NetworkConfig config;

    private final WaitForPort waitForPort;

    private final AtomicReferenceProvider<Proxy> proxyAtomicReferenceProvider = new AtomicReferenceProvider<>( null );

    private final AtomicReferenceProvider<ProxyRegistry> proxyRegistryAtomicReferenceProvider = new AtomicReferenceProvider<>( null );

    private final AtomicReferenceProvider<NetworkConfigRequestRegistry> networkConfigRequestRegistryProvider = new AtomicReferenceProvider<>( null );

    @Inject
    public NetworkService(NetworkConfig config, WaitForPort waitForPort) {
        this.config = config;
        this.waitForPort = waitForPort;
    }

    @Override
    public void init() {
        advertise( HttpRequestBuilder.class, ConfiguredHttpRequestBuilder.class );
        advertise( DirectHttpRequestBuilder.class, DefaultHttpRequestBuilder.class );
        advertise( ContentFetcher.class, DefaultContentFetcher.class );
        advertise( RequestListeners.class, new RequestListeners() );
        provider( Proxy.class, proxyAtomicReferenceProvider );
        provider( ProxyRegistry.class, proxyRegistryAtomicReferenceProvider );
        provider( NetworkConfigRequestRegistry.class, networkConfigRequestRegistryProvider );
    }

    @Override
    public void start() throws Exception {

        String defaultProxy = config.getDefaultProxy();

        if ( defaultProxy != null ) {

            NetworkConfig.Proxy proxyConfig = config.getProxies().get( defaultProxy );

            if ( proxyConfig == null ) {
                throw new RuntimeException( "Default proxy has no entry in proxies." );
            }

            ProxyReference proxyReference = createAndTestProxyReference( defaultProxy, proxyConfig );

            proxyAtomicReferenceProvider.set( proxyReference.getProxy() );

            info( "Using default proxy: %s", proxyReference.getProxy()  );

        }

        List<ProxyReference> proxyReferenceList = Lists.newArrayList();

        for (Map.Entry<String, NetworkConfig.Proxy> entry : config.getProxies().entrySet()) {

            String name = entry.getKey();
            NetworkConfig.Proxy proxyConfig = entry.getValue();

            proxyReferenceList.add( createAndTestProxyReference( name, proxyConfig ) );

        }

        ProxyRegistry proxyRegistry = new ProxyRegistry( proxyReferenceList );
        proxyRegistryAtomicReferenceProvider.set( proxyRegistry );

        NetworkConfigRequestRegistry networkConfigRequestRegistry = new NetworkConfigRequestRegistry( config.getRequests() );
        networkConfigRequestRegistryProvider.set( networkConfigRequestRegistry );

    }

    private ProxyReference createAndTestProxyReference(String name, NetworkConfig.Proxy proxyConfig) throws Exception {

        info( "Waiting for proxy on %s:%s", proxyConfig.getHost(), proxyConfig.getPort() );

        // *** make sure we can connect to the port that is open, if not we
        // need to fail.

        waitForPort.waitFor( proxyConfig.getHost(), proxyConfig.getPort(), TIMEOUT );

        SocketAddress addr = new InetSocketAddress( proxyConfig.getHost(), proxyConfig.getPort() );

        Proxy.Type type = Proxy.Type.HTTP;

        Proxy proxy = new Proxy( type, addr );

        return new ProxyReference( name, proxyConfig.getPriority(), proxyConfig.getRegex(), proxy );

    }

}