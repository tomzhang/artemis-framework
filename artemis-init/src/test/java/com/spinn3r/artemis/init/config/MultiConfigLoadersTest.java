package com.spinn3r.artemis.init.config;

import org.junit.Test;

import static org.junit.Assert.*;

public class MultiConfigLoadersTest {

    @Test
    public void test1() throws Exception {

        ConfigLoader configLoader
          = MultiConfigLoaders.create(new ResourceConfigLoader("." ),
                                      new ResourceConfigLoader( "/" ) );

        assertNotNull( configLoader.getResource( this.getClass(), "nested.conf" ) );
        assertNotNull( configLoader.getResource( this.getClass(), "root.conf" ) );

        assertNotNull( getClass().getResource( "nested.conf" ) );
        //assertNotNull( getClass().getResource( "root.conf" ) );

    }

}