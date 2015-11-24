package com.integratingfactor.idp.lib.userdetails;

import java.util.logging.Logger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class TestUserDetailsConfig {
    private static Logger LOG = Logger.getLogger(TestUserDetailsConfig.class.getName());

    @Bean
    public UserDetailsService userDetailsService() {
        LOG.info("Creating new bean instance TestUserDetailsServiceImpl");
        return new TestUserDetailsServiceImpl();
    }
}
