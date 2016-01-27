package com.integratingfactor.idp.lib.config;

import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

import com.integratingfactor.idp.lib.overrides.HttpSecurityWhiteLabelOverride;
import com.integratingfactor.idp.lib.overrides.IdpAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter implements ApplicationContextAware {
    private static Logger LOG = Logger.getLogger(SecurityConfig.class.getName());

    @Autowired
    PasswordEncoder passwordEncoder;

    private String defaultPassword;

    UserDetailsService userDetailsService;

    HttpSecurityWhiteLabelOverride override = null;
    
    IdpAuthenticationFilter customAuthentication = null;

    String filterUrl = null;

    ApplicationContext ctx;

    private void getOverrides() {
        userDetailsService = null;
        try {
            userDetailsService = ctx.getBean(UserDetailsService.class);
            LOG.info("Using custom application provided user details service: " + userDetailsService.toString());
        } catch (BeansException e) {
            LOG.info("No custom application provided user details service: " + e.getMessage());
        } catch (Exception e) {
            LOG.info("Caught exception: " + e.getMessage());
        }
        try {
            override = ctx.getBean(HttpSecurityWhiteLabelOverride.class);
            LOG.info("Using application overrides on white label URLs " + override.toString());
        } catch (BeansException e) {
            LOG.info("No custom application provided override service: " + e.getMessage());
        } catch (Exception e) {
            LOG.info("Caught exception: " + e.getMessage());
        }
        try {
            customAuthentication = ctx.getBean(IdpAuthenticationFilter.class);
            LOG.info("Found custom application provided authentication filter: " + customAuthentication.toString());
            filterUrl = customAuthentication.getFilterUrl();
        } catch (BeansException e) {
            LOG.info("No custom application provided authentication filter: " + e.getMessage());
        } catch (Exception e) {
            LOG.info("Caught exception: " + e.getMessage());
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        super.setApplicationContext(ctx);
        this.ctx = ctx;

        // get all overrides
        getOverrides();
    }

    @Override
    protected UserDetailsService userDetailsService() {
        if (userDetailsService != null) {
            LOG.info("Providing custom application user details service....");
            return userDetailsService;
        } else {
            LOG.info("Providing super's user details service....");
            return super.userDetailsService();
        }
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if (override == null) {
            LOG.info("using default security configuration");
            super.configure(http);
        } else {
            // start with adding permit all for public resources
            ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry urlPolicy = null;
            if (override.getPublicUrls() != null) {
                for (String url : override.getPublicUrls()) {
                    LOG.info("permitting unauthenticated access to: " + url);
                    urlPolicy = urlPolicy == null ? http.authorizeRequests().antMatchers(url).permitAll()
                            : urlPolicy.antMatchers(url).permitAll();
                }
            }
            if (filterUrl != null) {
                LOG.info("permitting unauthenticated access to filterUrl: " + filterUrl);
                urlPolicy = urlPolicy == null ? http.authorizeRequests().antMatchers(filterUrl).permitAll()
                        : urlPolicy.antMatchers(filterUrl).permitAll();
            }
            LOG.info("configuring form login ...");
            urlPolicy = urlPolicy == null ? http.authorizeRequests() : urlPolicy;
            urlPolicy.anyRequest().authenticated().and().formLogin();

            // override login page url if provided
            if (StringUtils.isNotEmpty(override.getLoginPageUrl())) {
                LOG.info("overriding white label login page url to: " + override.getLoginPageUrl()
                        + " permitting all..");
                http.formLogin().loginPage(override.getLoginPageUrl());
            }

            // override login processing url if provided
            if (StringUtils.isNotEmpty(override.getLoginProcessingUrl())) {
                LOG.info("overriding white label login processing url to: " + override.getLoginProcessingUrl());
                http.formLogin().loginProcessingUrl(override.getLoginProcessingUrl());
            }

            // override login success url if provided
            if (StringUtils.isNotEmpty(override.getDefaultSuccessUrl())) {
                LOG.info("overriding white label login success url to: " + override.getDefaultSuccessUrl());
                http.formLogin().defaultSuccessUrl(override.getDefaultSuccessUrl());
            }

            // override form login username parameter name if provided
            if (StringUtils.isNotEmpty(override.getUsernameParameter())) {
                LOG.info("overriding white label form login username parameter: " + override.getUsernameParameter());
                http.formLogin().usernameParameter(override.getUsernameParameter()).permitAll();
            }

            // override form login password parameter name if provided
            if (StringUtils.isNotEmpty(override.getPasswordParameter())) {
                LOG.info("overriding white label form login password parameter: " + override.getPasswordParameter());
                http.formLogin().passwordParameter(override.getPasswordParameter()).permitAll();
            }

            // override logout processing url if provided
            if (StringUtils.isNotEmpty(override.getLogoutUrl())) {
                LOG.info("overriding white label logout processing url to: " + override.getLogoutUrl());
                http.logout().logoutUrl(override.getLogoutUrl());
            }

            // override logout success url if provided
            if (StringUtils.isNotEmpty(override.getLogoutSuccessUrl())) {
                LOG.info("overriding white label logout success url to: " + override.getLogoutSuccessUrl());
                http.logout().logoutSuccessUrl(override.getLogoutSuccessUrl()).permitAll();
            }

            // add custom authentication filter if provided
            if (customAuthentication != null) {
                LOG.info("Adding custom authentication filter " + customAuthentication.getClass().getName() + " before "
                        + customAuthentication.getNextFilter().getName());
                http.addFilterBefore(customAuthentication, customAuthentication.getNextFilter());
            }
        }
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // check if there is custom user details service
        if (userDetailsService != null) {
            auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);
        } else {
            LOG.info("Using in-memory user details service with hard coded users: \"user\" and \"admin\"");
            setDefaultPassword(passwordEncoder.encode("password"));
            auth
                    // enable in memory based authentication with a user named
                    // "user" and "admin"
                    .inMemoryAuthentication().withUser("user").password(getDefaultPassword()).roles("USER").and()
                    .withUser("admin").password(getDefaultPassword()).roles("USER", "ADMIN").and()
                    .passwordEncoder(passwordEncoder);
        }
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    /**
     * register Spring Security with existing application context
     * 
     * @author gnulib
     *
     */
    public static class SecurityWebApplicationInitializer extends AbstractSecurityWebApplicationInitializer {

    }
}
