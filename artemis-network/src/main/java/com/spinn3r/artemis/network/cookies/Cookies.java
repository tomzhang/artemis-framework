package com.spinn3r.artemis.network.cookies;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.spinn3r.artemis.network.builder.HttpRequest;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class Cookies {

    public static final String SET_COOKIE_HEADER_NAME = "Set-Cookie";

    public static ImmutableMap<String,Cookie> fromHttpRequest( HttpRequest httpRequest ) {
        return fromResponseHeadersMap( httpRequest.getResponseHeadersMap() );
    }

    public static ImmutableMap<String,Cookie> fromResponseHeadersMap( ImmutableMap<String,ImmutableList<String>> responseHeadersMap ) {
        return fromSetCookiesList( responseHeadersMap.get( SET_COOKIE_HEADER_NAME ) );
    }

    public static ImmutableMap<String,Cookie> fromResponseHeadersMap( Map<String, List<String>> responseHeadersMap ) {
        return fromSetCookiesList( responseHeadersMap.get( SET_COOKIE_HEADER_NAME ) );
    }

    public static ImmutableMap<String,Cookie> fromSetCookiesList( List<String> setCookies ) {

        Map<String,Cookie> cookies = Maps.newHashMap();

        if ( setCookies == null || setCookies.size() == 0 ) {
            return ImmutableMap.copyOf( cookies );
        }

        for (String setCookie : setCookies) {

            Cookie cookie = CookieDecoder.decode( setCookie );

            if ( cookie == null )
                continue;


            cookies.put( cookie.getName(), cookie );

        }

        return ImmutableMap.copyOf( cookies );

    }

}
