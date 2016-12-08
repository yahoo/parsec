package {packageName};

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import {packageName}.parsec_generated.ParsecApplication;

/**
 * This is the entry point of Jersey, which is defined in web.xml.
 */
@SuppressWarnings("unused")
public class DefaultApplication extends ParsecApplication {

    /**
     * Default constructor.
     */
    public DefaultApplication() {
        // Parsec default bindings and registers
        super();

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                // Add additional binding here
                // bind(<implementation>.class).to(<interface>.class)
            }
        });

        // Add additional register here
        // register(<resource>.class)
    }
}
