package {packageName}.parsec_generated;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.moxy.json.MoxyJsonConfig;
import org.eclipse.persistence.jaxb.BeanValidationMode;
import org.eclipse.persistence.jaxb.MarshallerProperties;

{imports}

/**
 * Parsec Application
 */
@SuppressWarnings("unused")
public class ParsecApplication extends ResourceConfig {

    /**
     * Default constructor.
     */
    public ParsecApplication() {
        register(new AbstractBinder() {
            @Override
            protected void configure() {
{binding}
            }
        });

        // disable bean validation for output
        register(new MoxyJsonConfig()
            .property(MarshallerProperties.BEAN_VALIDATION_MODE, BeanValidationMode.NONE).resolver());

{register}
    }
}
