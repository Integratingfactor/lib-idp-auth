package com.integratingfactor.idp.lib.config;

import java.util.logging.Logger;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter implements ApplicationContextAware {
    private static Logger LOG = Logger.getLogger(SecurityConfig.class.getName());

    @Autowired
    PasswordEncoder passwordEncoder;

    private String defaultPassword;

    UserDetailsService userDetailsService;

    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        super.setApplicationContext(ctx);
        userDetailsService = null;
        try {
            userDetailsService = ctx.getBean(UserDetailsService.class);
            LOG.info("Using custom application provided user details service: " + userDetailsService.toString());
        } catch (BeansException e) {
            LOG.info("No custom application provided user details service: " + e.getMessage());
        } catch (Exception e) {
            LOG.info("Caught exception: " + e.getMessage());
        }
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
