package com.spinn3r.artemis.http;

import com.google.inject.Inject;
import com.spinn3r.artemis.http.servlets.HelloServlet;
import com.spinn3r.artemis.http.servlets.RequestMetaServlet;
import com.spinn3r.artemis.init.Launcher;
import com.spinn3r.artemis.init.MockVersionService;
import com.spinn3r.artemis.init.ServiceReferences;
import com.spinn3r.artemis.logging.init.ConsoleLoggingService;
import com.spinn3r.artemis.network.builder.HttpRequestBuilder;
import com.spinn3r.artemis.network.init.DirectNetworkService;
import com.spinn3r.artemis.test.BaseTestWithCapturedOutput;
import com.spinn3r.artemis.init.MockHostnameService;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.*;

import static com.jayway.awaitility.Awaitility.*;

public class ServerBuilderTest extends BaseTestWithCapturedOutput {

    private static final int PORT = 8081;

    private Server server;

    Launcher launcher;

    @Inject
    HttpRequestBuilder httpRequestBuilder;

    @Override
    @Before
    public void setUp() throws Exception {

        super.setUp();

        launcher = Launcher.forResourceConfigLoader().build();

        launcher.launch( new TestServiceReferences() );

        launcher.getInjector().injectMembers( this );
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();

        System.out.printf( "Stopping server..." );

        this.server.stop();
    }

    @Test
    @Ignore
    public void testRequestMeta() throws Exception {

        this.server = new ServerBuilder()
                        .setPort( PORT )
                        .addServlet( "/", new RequestMetaServlet() )
                        .build();

        this.server.start();

        String link = String.format( "http://localhost:%s/", PORT );
        String content = httpRequestBuilder.get( link ).execute().getContentWithEncoding();

        System.out.printf( "content: %s\n", content );

        await().until( () -> {
            assertTrue( out.toString().contains( "GET / HTTP/1.1" ) );
            assertTrue( out.toString().contains( "org.eclipse.jetty.server.RequestLog 127.0.0.1" ) );
            assertTrue( out.toString().contains( "{HTTP/1.1}{0.0.0.0:8081}" ) );

        } );

    }


    @Test
    public void test1() throws Exception {

        this.server = new ServerBuilder()
                        .setPort( PORT )
                        .addServlet( "/", new HelloServlet() )
                        .build();

        this.server.start();

        String link = String.format( "http://localhost:%s/", PORT );
        String content = httpRequestBuilder.get( link ).execute().getContentWithEncoding();

        System.out.printf( "content: %s\n", content );

        assertEquals( "hello world", content );

        await().until( () -> {
            assertTrue( out.toString().contains( "GET / HTTP/1.1" ) );
            assertTrue( out.toString().contains( "org.eclipse.jetty.server.RequestLog 127.0.0.1" ) );
            assertTrue( out.toString().contains( "{HTTP/1.1}{0.0.0.0:8081}" ) );

        } );

    }


    @Test
    public void testUseLocalhost() throws Exception {

        this.server = new ServerBuilder()
                        .setPort( PORT )
                        .setUseLocalhost( true )
                        .addServlet( "/", new HelloServlet() )
                        .build();

        this.server.start();

        String link = String.format( "http://localhost:%s/", PORT );
        String content = httpRequestBuilder.get( link ).execute().getContentWithEncoding();

        System.out.printf( "content: %s\n", content );

        assertEquals( "hello world", content );

        await().until( () -> {
            assertTrue( out.toString().contains( "GET / HTTP/1.1" ) );
            assertTrue( out.toString().contains( "org.eclipse.jetty.server.RequestLog 127.0.0.1" ) );
            assertTrue( out.toString().contains( "{HTTP/1.1}{127.0.0.1:8081}" ) );
        } );

    }

    static class TestServiceReferences extends ServiceReferences {

        public TestServiceReferences() {
            add( MockVersionService.class );
            add( MockHostnameService.class );
            add( ConsoleLoggingService.class );
            add( DirectNetworkService.class );
        }

    }

}