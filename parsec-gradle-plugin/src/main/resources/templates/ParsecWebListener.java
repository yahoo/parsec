package {packageName}.parsec_generated;

import org.glassfish.jersey.servlet.ServletContainer;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;

import {packageName}.DefaultApplication;

/**
 * Parsec Web listener.
 */
public class ParsecWebListener implements ServletContextListener {

    /**
     * default web application servlet const.
     */
    protected static final String DEFAULT_WEBAPP = "Web Application";
    /**
     * swagger ui servlet const.
     */
    protected static final String SWAGGER_UI = "Swagger UI";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        ServletContainer servlet = new ServletContainer(new DefaultApplication());
        ServletRegistration.Dynamic defaultWebapp = context.addServlet(DEFAULT_WEBAPP, servlet);

        if (null != defaultWebapp) {
            defaultWebapp.addMapping("/*");
            defaultWebapp.setLoadOnStartup(1);
        }

        ParsecWrapperServlet swaggerServlet = new ParsecWrapperServlet();
        ServletRegistration.Dynamic swagger = context.addServlet(SWAGGER_UI, swaggerServlet);

        if (null != swagger) {
            swagger.addMapping("/static/*");
        }
    }

    @SuppressWarnings("PMD.UncommentedEmptyMethod")
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
