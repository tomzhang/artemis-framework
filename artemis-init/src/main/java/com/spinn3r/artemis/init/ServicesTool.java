package com.spinn3r.artemis.init;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.spinn3r.artemis.init.tracer.Tracer;
import com.spinn3r.artemis.init.tracer.TracerFactory;

import java.util.Collections;
import java.util.List;

/**
 *
 */
public class ServicesTool {

    protected boolean verbose = false;

    private Launcher launcher;

    private Services services;

    public ServicesTool( Launcher launcher, Services services ) {

        this.launcher = launcher;
        this.services = services;

    }

    /**
     * Perform init on all services for this services tool..
     */
    public void init() {

        for (Service service : services) {

            init( service );

        }

    }

    /**
     * Init a specific service.
     *
     * @param service
     */
    public void init(Service service) {

        Advertised advertised = launcher.advertised;

        TracerFactory tracerFactory = advertised.tracerFactorySupplier.get();

        ServiceReference serviceReference = new ServiceReference( service.getClass() );

        service.setAdvertised( advertised );
        service.setTracer( tracerFactory.create( service ) );
        service.setConfigLoader( launcher.getConfigLoader() );
        service.setServiceCache(launcher.getServiceCache());
        service.setIncluder( new Includer( launcher, serviceReference ) );

        ServiceInitializer serviceInitializer = new ServiceInitializer( launcher );
        serviceInitializer.init( serviceReference );
        service.init();

    }

    /**
     * Start all services.
     *
     * @throws Exception
     */
    public void start() throws Exception {

        for (Service service : services) {

            Stopwatch stopwatch = Stopwatch.createStarted();

            TracerFactory tracerFactory = launcher.advertised.tracerFactorySupplier.get();

            Tracer tracer = tracerFactory.create( launcher );

            tracer.info( "Starting service: %s ...", service.getClass().getName() );

            try {

                service.start();

            } catch ( Exception e ) {
                tracer.error("Failed to start: " + service.getClass().getName(), e );
                throw e;
            }

            tracer.info( "Starting service: %s ...done (%s)", service.getClass().getName(), stopwatch.stop() );

        }

    }

    /**
     * Stop the services in reverse order.  We need to do reverse order because
     * later services depend on previous services.
     *
     * @throws Exception
     */
    public void stop() throws Exception {

        // reverse the list so that we can

        List<Service> reverse = Lists.newArrayList( services );
        Collections.reverse( reverse );

        for (Service service : services) {

            TracerFactory tracerFactory = launcher.advertised.tracerFactorySupplier.get();
            Tracer tracer = tracerFactory.create( launcher );

            tracer.info( "Stopping service: %s ...", service.getClass().getName() );

            Stopwatch stopwatch = Stopwatch.createStarted();

            service.stop();

            tracer.info( "Stopping service: %s ...done (%s)", service.getClass().getName(), stopwatch.stop() );

        }

    }

}
