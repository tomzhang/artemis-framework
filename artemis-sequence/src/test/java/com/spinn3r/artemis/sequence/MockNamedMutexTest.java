package com.spinn3r.artemis.sequence;

import org.junit.Test;

public class MockNamedMutexTest {

    @Test
    public void testBasicAcquireThenRelease() throws Exception {

        NamedMutexFactory namedMutexFactory = new MockNamedMutexFactory();
        NamedMutex namedMutex = namedMutexFactory.acquire( "testmutex" );
        namedMutex.close();

    }

    @Test( expected = NamedMutexException.AcquireException.class )
    public void testDoubleAcquireWithFailure() throws Exception {

        NamedMutexFactory namedMutexFactory = new MockNamedMutexFactory();

        namedMutexFactory.acquire( "testmutex" );
        namedMutexFactory.acquire( "testmutex" );

    }

    @Test( expected = NamedMutexException.AlreadyReleasedException.class )
    public void testAcquireWithFailureDueToDoubleRelease() throws Exception {

        NamedMutexFactory namedMutexFactory = new MockNamedMutexFactory();

        NamedMutex namedMutex = namedMutexFactory.acquire( "testmutex" );
        namedMutex.close();
        namedMutex.close();

    }



}