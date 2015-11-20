package com.integratingfactor.idp.lib.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

@ContextConfiguration(classes={SecurityConfig.class})
@WebAppConfiguration
public class SecurityConfigTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private AuthenticationManager authManager;

    @Test
    public void securityConfigurationLoads() {
        Assert.assertNotNull(authManager);
        Assert.assertNotNull(springSecurityFilterChain);
    }

}
