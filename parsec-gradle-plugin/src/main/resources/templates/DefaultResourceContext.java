package {packageName};

import {packageName}.parsec_generated.ResourceContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ResourceContext default implementation.
 */
public class DefaultResourceContext implements ResourceContext {
    /**
     * Request.
     */
    private HttpServletRequest request;

    /**
     * Response.
     */
    private HttpServletResponse response;

    /**
     * Constructor.
     * @param request Request
     * @param response Response
     */
    public DefaultResourceContext(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    /**
     * Get request.
     * @return Request
     */
    @Override
    public HttpServletRequest request() {
        return request;
    }

    /**
     * Get response.
     * @return Response
     */
    @Override
    public HttpServletResponse response() {
        return response;
    }

    /**
     * Authenticate.
     */
    @Override
    public void authenticate() {
        // Unused
    }

    /**
     * Authorize.
     * @param action Action
     * @param resource Resource
     * @param trustedDomain Trusted domain
     */
    @Override
    public void authorize(String action, String resource, String trustedDomain) {
        // Unused
    }
}
