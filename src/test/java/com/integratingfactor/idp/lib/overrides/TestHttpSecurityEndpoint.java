package com.integratingfactor.idp.lib.overrides;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * a simple test implementation to override white label URLs for unit testing
 * and implement HTTP endpoints for custom URLs
 * 
 * @author gnulib
 *
 */
@Controller
public class TestHttpSecurityEndpoint implements HttpSecurityWhiteLabelOverride {

    public static final String LoginPageUrl = "/test/login";

    public static final String UsernameParameter = "testUsernameName";

    public static final String PasswordParameter = "testPasswordName";

    public static final String LoginProcessingUrl = "/test/login.do";

    public static final String LoginSuccessUrl = "/test/login/success";

    public static final String LoginFailureUrl = "/test/login/failure";

    public static final String LogoutUrl = "/test/logout.do";

    public static final String LogoutSuccessUrl = "/test/logout/success";

    public static final String ProtectedResource = "/test/something/secret";

    @Override
    public String getLoginPageUrl() {
        return LoginPageUrl;
    }

    /**
     * HTTP endpoint to handle login page request on the custom login url
     * 
     * @see {@link org.springframework.security.web.authentication.ui.DefaultLoginPageGeneratingFilter}
     * @param request
     *            incoming HTTP request
     * @return a login form
     */
    @RequestMapping(value = TestHttpSecurityEndpoint.LoginPageUrl, method = RequestMethod.GET)
    public @ResponseBody String generateLoginPage(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();

        sb.append("<html><head><title>Login Page</title></head>");

        sb.append("<body onload='document.f.").append(UsernameParameter).append(".focus();'>\n");

        sb.append("<h3>Login with Username and Password</h3>");
        sb.append("<form name='f' action='").append(request.getContextPath()).append(LoginProcessingUrl)
                .append("' method='POST'>\n");
        sb.append("<table>\n");
        sb.append(" <tr><td>User:</td><td><input type='text' name='");
        sb.append(UsernameParameter).append("' value='").append("'></td></tr>\n");
        sb.append(" <tr><td>Password:</td><td><input type='password' name='").append(PasswordParameter)
                .append("'/></td></tr>\n");

        sb.append(" <tr><td colspan='2'><input name=\"submit\" type=\"submit\" value=\"Login\"/></td></tr>\n");
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());

        if (token != null) {
            sb.append(" <input name=\"" + token.getParameterName() + "\" type=\"hidden\" value=\"" + token.getToken()
                    + "\" />\n");
        }

        sb.append("</table>\n");
        sb.append("</form>");

        sb.append("</body></html>");

        return sb.toString();
    }

    @Override
    public String getUsernameParameter() {
        return UsernameParameter;
    }

    @Override
    public String getPasswordParameter() {
        return PasswordParameter;
    }

    @Override
    public String getLoginProcessingUrl() {
        return LoginProcessingUrl;
    }

    @Override
    public String getDefaultSuccessUrl() {
        return LoginSuccessUrl;
    }

    /**
     * HTTP endpoint to handle landing page after successful login
     * 
     * @param request
     *            incoming HTTP request
     * @return a welcome HTML page
     */
    @RequestMapping(value = TestHttpSecurityEndpoint.LoginSuccessUrl, method = RequestMethod.GET)
    public @ResponseBody String afterLogin() {
        StringBuilder sb = new StringBuilder();

        sb.append("<html><head><title>Login Success</title></head>");

        sb.append("<body>\n");

        sb.append("<h3>Welcome</h3>");
        sb.append("Login Success");
        sb.append("</body></html>");

        return sb.toString();
    }

    @Override
    public String getFailureUrl() {
        return LoginFailureUrl;
    }

    /**
     * HTTP endpoint to handle login failure
     * 
     * @param request
     *            incoming HTTP request
     * @return an error HTML page
     */
    @RequestMapping(value = TestHttpSecurityEndpoint.LoginFailureUrl, method = RequestMethod.GET)
    public @ResponseBody String handleLoginFailure() {
        StringBuilder sb = new StringBuilder();

        sb.append("<html><head><title>Login Failure</title></head>");

        sb.append("<body>\n");

        sb.append("<h3>Login Failure!!!</h3>");
        sb.append("Username/Password did not match");
        sb.append("</body></html>");

        return sb.toString();
    }

    @Override
    public String getLogoutUrl() {
        return LogoutUrl;
    }

    @Override
    public String getLogoutSuccessUrl() {
        return LogoutSuccessUrl;
    }

    /**
     * HTTP endpoint to handle landing page after logout
     * 
     * @param request
     *            incoming HTTP request
     * @return a generic HTML page
     */
    @RequestMapping(value = TestHttpSecurityEndpoint.LogoutSuccessUrl, method = RequestMethod.GET)
    public @ResponseBody String afterLogout() {
        StringBuilder sb = new StringBuilder();

        sb.append("<html><head><title>Logout Success</title></head>");

        sb.append("<body>\n");

        sb.append("<h3>Logout Success!!!</h3>");
        sb.append("You have been logged out.");
        sb.append("</body></html>");

        return sb.toString();
    }

    /**
     * HTTP endpoint to handle requests for a protected resource
     * 
     * @param request
     *            incoming HTTP request
     * @return a generic HTML page
     */
    @RequestMapping(value = TestHttpSecurityEndpoint.ProtectedResource, method = RequestMethod.GET)
    public @ResponseBody String getSecret() {
        StringBuilder sb = new StringBuilder();

        sb.append("<html><head><title>Secret</title></head>");

        sb.append("<body>\n");

        sb.append("<h3>Secret Info!!!</h3>");
        sb.append("This is protected.");
        sb.append("</body></html>");

        return sb.toString();
    }
}
