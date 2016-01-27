package com.integratingfactor.idp.lib.overrides;

/**
 * <p>
 * Strategy used to override default white label URLs used by Spring Security
 * framework
 * </p>
 * <p>
 * Implementations can provide non null non empty values for any of the
 * configurations that need to be overridden. Any null or empty return value
 * will result in using default white label configuration for that particular
 * URL.
 * </p>
 * 
 * @author gnulib
 *
 */
public interface HttpSecurityWhiteLabelOverride {

    /**
     * method to provide an array of url patterns that can be allowed without
     * any authentication, e.g., resource files, login page, welcome/landing
     * page etc.
     * 
     * @return
     */
    String[] getPublicUrls();

    /**
     * <p>
     * method to override white label login page url "/login". Application is
     * required to process the specified URL to generate a login page.
     * </p>
     * <p>
     * Any unsuccessful authentication will also get redirected to same URL with
     * additional parameter "?error"
     * </p>
     * <p>
     * <strong>Note: if custom login page url is used, then login processing url
     * will also be set to the same, unless a different url is explicitly
     * specified with {@link #getLoginProcessingUrl()}
     * 
     * @see {@link org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer#loginPage(String)}
     * 
     * @return login page url override
     */
    String getLoginPageUrl();

    /**
     * method to override white label username parameter name "username"
     * 
     * @see {@link org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer#usernameParameter(String)}
     * 
     * @return username parameter name override
     */
    String getUsernameParameter();

    /**
     * method to override white label password parameter name "password"
     * 
     * @see {@link org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer#passwordParameter(String)}
     * 
     * @return password parameter name override
     */
    String getPasswordParameter();

    /**
     * method to override default white label url for processing the
     * credentials. Internally spring security will change filter to look at
     * this different value. Application does not need to implement any handling
     * at this location.
     * 
     * @see {@link org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter}
     * @see {@link org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter}
     * 
     * @return login processing url override
     */
    String getLoginProcessingUrl();

    /**
     * <p>
     * method to override default while label url where users are redirected
     * after successful authentication. Application is required to process the
     * specified URL.
     * </p>
     * <p>
     * If application needs to implement a different success handler (e.g., for
     * openid connect to redirect after authentication) then need to override
     * the success handler directly instead of changing just this white label
     * url.
     * </p>
     * 
     * @see {@link org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer#defaultSuccessUrl(String)}
     * @see {@link org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer#successHandler(org.springframework.security.web.authentication.AuthenticationSuccessHandler)}
     * @see {@link org.springframework.security.web.authentication.AuthenticationSuccessHandler}
     * @return url path to redirect to
     */
    String getDefaultSuccessUrl();

    /**
     * <p>
     * method to override default while label url where users are redirected
     * after authentication failure. Application is required to process the
     * specified URL.
     * </p>
     * 
     * @see {@link org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer#failureUrl(String)}
     * @return url path to redirect to
     */
    String getFailureUrl();

    /**
     * method to override default white label URL for initiating logout.
     * Internally spring security will change filter to look at this different
     * value. Application does not need to implement any handling at this
     * location. However, as mentioned in spring security documentation, HTTP
     * action must be POST to this logout URL.
     * 
     * @see {@link org.springframework.security.config.annotation.web.configurers.LogoutConfigurer#logoutUrl(String)}
     * @see {@link org.springframework.security.web.authentication.logout.LogoutFilter}
     * 
     * @return login processing url override
     */
    String getLogoutUrl();

    /**
     * <p>
     * method to override default while label url where users are redirected
     * after logout. Application is required to process the specified URL.
     * </p>
     * 
     * @see {@link org.springframework.security.config.annotation.web.configurers.LogoutConfigurer#logoutSuccessUrl(String)}
     * @return url path to redirect to
     */
    String getLogoutSuccessUrl();

}
