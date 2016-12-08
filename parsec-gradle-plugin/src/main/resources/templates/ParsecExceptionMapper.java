package {packageName}.parsec_generated;

import java.util.Arrays;
import java.util.List;

import com.yahoo.parsec.logging.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Arrays;
import com.google.common.base.Throwables;

/**
 * Parsec exception mapper for uncaught RuntimeException.
 */
public class ParsecExceptionMapper implements ExceptionMapper<RuntimeException> {

    /** the logger. */
    private static final Logger LOG = LoggerFactory.getLogger(ParsecExceptionMapper.class);

    /** the uncaught exception code. */
    public static final String UNCAUGHT_EXCEPTION_CODE = "parsec.config.exception.uncaughtExceptionCode";

    /** the config injected by Jsrsey by default. */
    private final Configuration config;

    /** the request. */
    @Context
    private HttpServletRequest request;

    /**
     * default constructor with config argument.
     *
     * @param config configuration
     */
    public ParsecExceptionMapper(Configuration config) {
        this.config = config;
    }

    /**
     * to response.
     * @param e runtime exception
     * @return response
     */
    @Override
    public Response toResponse(RuntimeException e) {
        int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        if (e instanceof WebApplicationException) {
            Response r = ((WebApplicationException) e).getResponse();
            status = r.getStatus();
        }
        Throwable rootCause = Throwables.getRootCause(e);
        logRuntimeException(e, status);
        String detail = rootCause.toString();
        Object entity = buildResourceError(detail, getExceptionCodeFromProperty());

        return Response.status(status).entity(entity).build();
    }

    /**
     * get default exception error code if property set.
     * @return exception error code.
     */
    private Integer getExceptionCodeFromProperty() {
        Integer errorCode = null;
        if (this.config != null) {
            Object code = this.config.getProperty(UNCAUGHT_EXCEPTION_CODE);
            if (code != null) {
                errorCode = Integer.parseInt(code.toString());
            }
        }
        return errorCode;
    }

    /**
     * log runtime exception.
     * @param e runtime exception
     * @param httpStatus http status code
     */
    private void logRuntimeException(RuntimeException e, int httpStatus) {
        Throwable rootCause = Throwables.getRootCause(e);
        List<Throwable> errStacks = Throwables.getCausalChain(e);
        Throwable[] errStacks2 = new Throwable[errStacks.size()];
        errStacks2 = Arrays.copyOfRange(errStacks.toArray(errStacks2), 0, 3);
        String errStack = Arrays.toString(errStacks2).replaceAll("[\n\r]", " ");
        String msg = rootCause.toString();

        // TODO: we may need to log request content as well,
        //   but there is no way to read it from request for now.
        String className = this.getClass().getSimpleName();
        Map<String, String> meta = new LinkedHashMap<>();
        meta.put("trace_tag", className);
        meta.put("http_code", String.valueOf(httpStatus));
        meta.put("uri", request.getRequestURL().toString());
        meta.put("trace_string", errStack);
        String logInfo = LogUtil.generateLog(className, msg, meta);

        if (httpStatus == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
            LOG.error(logInfo);
        } else {
            LOG.debug(logInfo);
        }
    }

    /**
     * build resource error.
     * @param message the error message
     * @param code the error code
     * @return ParsecResourceError
     */
    protected Object buildResourceError(final String message, final Integer code) {
        ParsecErrorBody errBody = new ParsecErrorBody();
        errBody.setMessage(message);
        if (code != null) {
            errBody.setCode(code);
        }
        ParsecResourceError resultErr = new ParsecResourceError();
        resultErr.setError(errBody);

        return resultErr;
    }
}
