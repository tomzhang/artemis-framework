package com.spinn3r.artemis.network;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.spinn3r.artemis.guava.ImmutableCollectors;
import com.spinn3r.artemis.init.LauncherTest;
import com.spinn3r.artemis.init.MockHostnameService;
import com.spinn3r.artemis.init.MockVersionService;
import com.spinn3r.artemis.logging.init.ConsoleLoggingService;
import com.spinn3r.artemis.network.builder.HttpRequestBuilder;
import com.spinn3r.artemis.network.builder.HttpRequestMethod;
import com.spinn3r.artemis.network.builder.HttpRequestMethods;
import com.spinn3r.artemis.network.cookies.Cookie;
import com.spinn3r.artemis.network.cookies.jar.CookieJarManager;
import com.spinn3r.artemis.network.init.DirectNetworkService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class WeiboCookiesTest extends LauncherTest {

    @Inject
    Provider<CookieJarManager> cookieJarManagerProvider;

    @Inject
    HttpRequestBuilder httpRequestBuilder;

    @Override
    @Before
    public void setUp() throws Exception {

        setServiceReferences(ImmutableList.of(MockVersionService.class,
                                              MockHostnameService.class,
                                              ConsoleLoggingService.class,
                                              DirectNetworkService.class));

        super.setUp();
    }

    @Test
    public void testWeiboCookies() throws Exception {

        assertEquals(30, cookieJarManagerProvider.get().getCookieJar("http://www.weibo.com").size());

        HttpRequestMethod httpRequestMethod = httpRequestBuilder.get("http://weibo.com");

        assertEquals("[SUB, SUBP]",
                     httpRequestMethod.getCookies().stream().map(Cookie::getName).collect(ImmutableCollectors.toImmutableList()).toString());

    }

}