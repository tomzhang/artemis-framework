package com.spinn3r.artemis.http.init;

import com.google.inject.Inject;
import com.spinn3r.artemis.http.*;
import com.spinn3r.artemis.init.BaseService;
import com.spinn3r.artemis.init.Config;
import org.eclipse.jetty.server.Server;

/**
 * HTTP webserver for our admin interface.
 */
@Config( path = "webserver.conf",
         required = true,
         implementation = WebserverConfig.class )
public class WebserverService extends BaseService {

    private Server server;

    protected ServerBuilder serverBuilder = new ServerBuilder();

    protected final WebserverConfig webserverConfig;

    protected final ServletReferences servletReferences;

    protected final FilterReferences filterReferences;

    protected final RequestLogReferences requestLogReferences;

    @Inject
    WebserverService(WebserverConfig webserverConfig, ServletReferences servletReferences, FilterReferences filterReferences, RequestLogReferences requestLogReferences) {
        this.webserverConfig = webserverConfig;
        this.servletReferences = servletReferences;
        this.filterReferences = filterReferences;
        this.requestLogReferences = requestLogReferences;
    }

    @Override
    public void start() throws Exception {

        info( "Starting HTTP server on port %s with maxThreads=%s...",  webserverConfig.getPort(), webserverConfig.getMaxThreads() );

        serverBuilder = new ServerBuilder()
            .setPort( webserverConfig.getPort() )
            .setMaxThreads( webserverConfig.getMaxThreads() );

        for (ServletReference servletReference : servletReferences) {

            info( "Adding %s as %s", servletReference.getPathSpec(), servletReference.getServletHolder().getClassName() );

            serverBuilder.addServlet( servletReference.getPathSpec(), servletReference.getServletHolder() );

        }

        for (FilterReference filterReference : filterReferences) {

            info( "Adding filter %s as %s", filterReference.getPathSpec(), filterReference.getFilterHolder().getClassName() );

            serverBuilder.addFilter( filterReference );

        }

        serverBuilder.setRequestLogReferences( requestLogReferences );
        serverBuilder.setUseLocalhost( webserverConfig.getUseLocalHost() );
        serverBuilder.setRequestHeaderSize( webserverConfig.getRequestHeaderSize() );
        serverBuilder.setResponseHeaderSize( webserverConfig.getResponseHeaderSize() );

        server = serverBuilder.build();

        try {
            server.start();
        } catch ( Exception e ) {
            throw new Exception( "Unable to start webserver on port: " + webserverConfig.getPort(), e );
        }

    }

    @Override
    public void stop() throws Exception {

        if ( server != null ) {
            server.stop();
        }

    }

}
