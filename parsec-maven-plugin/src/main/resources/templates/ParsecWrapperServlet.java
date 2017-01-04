package {packageName}.parsec_generated;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 * default servlet for static page (swagger, api json schema end point).
 */
public class ParsecWrapperServlet extends HttpServlet {

    /**
     *
     * @param req  HttpServletRequest
     * @param resp HttpServletResponse
     * @throws ServletException Exception
     * @throws IOException  Exception
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        RequestDispatcher rd = getServletContext().getNamedDispatcher("default");

        if (null != rd) {
            HttpServletRequest wrapped = new HttpServletRequestWrapper(req) {
                public String getServletPath() {
                    return "";
                }
            };

            rd.forward(wrapped, resp);
        }
    }
}
