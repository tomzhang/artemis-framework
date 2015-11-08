package com.spinn3r.artemis.init;

import com.google.inject.ConfigurationException;
import com.google.inject.CreationException;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.spinn3r.artemis.init.advertisements.Role;
import com.spinn3r.artemis.init.config.ConfigLoader;
import com.spinn3r.artemis.init.config.ResourceConfigLoader;
import com.spinn3r.artemis.init.threads.ThreadDiff;
import com.spinn3r.artemis.init.threads.ThreadSnapshot;
import com.spinn3r.artemis.init.threads.ThreadSnapshots;
import com.spinn3r.artemis.init.tracer.Tracer;
import com.spinn3r.artemis.init.tracer.TracerFactory;

import java.util.Iterator;

/**
 *
 * Launches all services for a daemon.
 *
 */
public class Launcher {

    private ConfigLoader configLoader;

    private final Services services = new Services();

    private final Advertised advertised;

    private final AtomicReferenceProvider<Lifecycle> lifecycleProvider
      = new AtomicReferenceProvider<>( Lifecycle.STOPPED );

    // taken before launch() and after stop() to help detect improperly
    // shutdown services.
    private ThreadSnapshot threadSnapshot = new ThreadSnapshot();

    private ServiceReferences started = new ServiceReferences();

    /**
     * The main injector to use after launch is called.
     */
    private Injector injector = null;

    private ServiceReferences serviceReferences;

    public Launcher(ConfigLoader configLoader, Advertised advertised ) {
        this.configLoader = configLoader;
        this.advertised = advertised;

        // advertise myself so I can inject it if we want a command to be able
        // to call stop on itself after being launched.
        advertise( Launcher.class, this );
        provider( Lifecycle.class, lifecycleProvider );
    }

    /**
     * Perform basic init. Mostly used so that test code can verify that
     * bindings are working properly.
     *
     */
    public Launcher init( ServiceReferences references ) throws Exception {
        return launch( references, (servicesTool) -> { servicesTool.init(); } );
    }

    public Launcher launch() throws Exception {
        return launch( new ServiceReferences() );
    }

    public Launcher launch( ServiceReference... references ) throws Exception {

        return launch( new ServiceReferences( references ) );

    }

    public Launcher launch( ServiceReferences references ) throws Exception {

        return launch( references, (servicesTool) -> {
                servicesTool.init();
                servicesTool.start();
        } );

    }

    @SuppressWarnings("varargs")
    public Launcher launch( Class<? extends Service>... services ) throws Exception {

        ServiceReferences serviceReferences = new ServiceReferences();

        for (Class<? extends Service> service : services) {
            serviceReferences.add( service );
        }

        return launch( serviceReferences );

    }

    /**
     * Launch services by class.  This allows us to use dependency injection to
     * instantiate each one.
     *
     */
    public Launcher launch( ServiceReferences serviceReferences, LaunchHandler launchHandler ) throws Exception {

        this.serviceReferences = serviceReferences;

        info( "Launching services: \n%s", serviceReferences.format() );

        lifecycleProvider.set( Lifecycle.STARTING );

        ServiceInitializer serviceInitializer = new ServiceInitializer( this );

        // we iterate with a for loop so that if a service includes another
        // service we can incorporate it into our pipeline.
        for (int i = 0; i < serviceReferences.size(); i++) {

            ServiceReference serviceReference = serviceReferences.get( i );

            try {

                serviceInitializer.init( serviceReference );

                Injector injector = getAdvertised().createInjector();
                Service current = injector.getInstance( serviceReference.getBacking() );
                launch0( launchHandler, current );

                services.add( current );
                started.add( serviceReference );

            } catch ( ConfigurationException|CreationException e ) {

                String message = String.format( "Could not create service %s.  \n\nStarted services are: \n%s\nAdvertised bindings are: \n%s",
                                                serviceReference.getBacking().getName(), started.format(), advertised.format() );

                throw new Exception( message, e );

            }

        }

        if ( injector == null ) {
            // only ever done if we're starting without any services, usually
            // testing.
            injector = createInjector();
        }

        info( "Now running with the following advertisements: \n%s", advertised.format() );

        lifecycleProvider.set( Lifecycle.STARTED );

        return this;

    }

    public void launch0( LaunchHandler launchHandler, Service... services  ) throws Exception {
        launch0( launchHandler, new Services( services ) );
    }

    public void launch0( LaunchHandler launchHandler, Services newServices ) throws Exception {

        threadSnapshot.addAll( ThreadSnapshots.create() );

        if ( advertised.find( ConfigLoader.class ) == null ) {
            advertised.advertise( this, ConfigLoader.class, configLoader );
        }

        this.services.addAll( newServices );

        ServicesTool servicesTool = new ServicesTool( this, newServices );

        launchHandler.onLaunch( servicesTool );

        injector = createInjector();

    }

    /**
     * Stop all services started on this launcher.
     *
     * @return
     */
    public Launcher stop() throws Exception {

        lifecycleProvider.set( Lifecycle.STOPPING );

        new ServicesTool( this, services ).stop();

        lifecycleProvider.set( Lifecycle.STOPPED );

        ThreadDiff threadDiff = ThreadSnapshots.diff( threadSnapshot, ThreadSnapshots.create() );

        threadDiff.report( advertised.require( TracerFactory.class ).newTracer( this ) );

        threadSnapshot = new ThreadSnapshot();

        return this;
    }

    public ThreadSnapshot getThreadSnapshot() {
        return threadSnapshot;
    }

    public Services getServices() {
        return services;
    }

    public Advertised getAdvertised() {
        return advertised;
    }

    public ServiceReferences getServiceReferences() {
        return serviceReferences;
    }

    public void include( ServiceReference currentServiceReference, Class<? extends Service> includedService ) {

        int index = this.serviceReferences.indexOf( currentServiceReference );

        if ( index == -1 ) {
            throw new RuntimeException( String.format( "Could not find index of: %s in %s", currentServiceReference, serviceReferences ) );
        }

        this.serviceReferences.add( index + 1, new ServiceReference( includedService ) );

    }

    public <T> void provider( Class<T> clazz, Provider<T> provider ) {
        getAdvertised().provider( this.getClass(), clazz, provider );
    }

    public <T,V extends T> void advertise( Class<T> clazz, Class<V> impl ) {
        getAdvertised().advertise( this, clazz, impl );
    }

    public <T, V extends T> void advertise( Class<T> clazz, V object ) {
        getAdvertised().advertise( this, clazz, object );
    }

    public <T, V extends T> void replace( Class<T> clazz, V object ) {
        getAdvertised().replace( this, clazz, object );
    }

    public void verify() {
        getAdvertised().verify();;
    }

    public Injector createInjector() {
        return getAdvertised().createInjector();
    }

    public Injector getInjector() {
        return injector;
    }

    public <T> T getInstance( Class<T> clazz ) {
        return createInjector().getInstance( clazz );
    }

    public ConfigLoader getConfigLoader() {
        return configLoader;
    }

    private Tracer getTracer() {
        TracerFactory tracerFactory = advertised.require( TracerFactory.class );
        return tracerFactory.newTracer( this );
    }

    public void info( String format, Object... args ) {
        getTracer().info( format, args );
    }

    public void error( String format, Object... args ) {
        getTracer().error( format, args );
    }

    public static LauncherBuilder forConfigLoader( ConfigLoader configLoader ) {
        return new LauncherBuilder( configLoader );
    }

    public static LauncherBuilder forResourceConfigLoader() {
        return new LauncherBuilder( new ResourceConfigLoader() );
    }

    public static class LauncherBuilder {

        private ConfigLoader configLoader;

        private Role role = new Role( "default" );

        private Advertised advertised = new Advertised();

        LauncherBuilder(ConfigLoader configLoader) {
            this.configLoader = configLoader;
        }

        public LauncherBuilder withAdvertised( Advertised advertised ) {
            this.advertised = advertised;
            return this;
        }

        public LauncherBuilder withRole( String role ) {
            return withRole( new Role( role ) );
        }

        public LauncherBuilder withRole( Role role ) {
            this.role = role;
            return this;
        }

        public Launcher build() {

            Launcher result = new Launcher( configLoader, advertised );

            result.getAdvertised().advertise( this, Role.class, role );

            return result;

        }

    }

}
