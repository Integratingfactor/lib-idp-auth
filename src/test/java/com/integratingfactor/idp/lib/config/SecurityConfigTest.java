package com.integratingfactor.idp.lib.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

@ContextConfiguration(classes = { SecurityConfig.class })
@WebAppConfiguration
public class SecurityConfigTest extends AbstractTestNGSpringContextTests {

    @Autowired
    SecurityConfig securityConfig;
    
    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    public void securityConfigurationLoads() {
        Assert.assertNotNull(authManager);
        Assert.assertNotNull(springSecurityFilterChain);
    }

    /**
     * verify that BCrypt password encoder bean is configured
     */
    @Test
    public void testPasswordEncoderBeanConfiguration() {
        // make sure the password encoder bean is available in application
        // conext
        Assert.assertNotNull(passwordEncoder);

        // make sure this password encoder is of type BCryptPasswordEncoder
        Assert.assertTrue(passwordEncoder instanceof BCryptPasswordEncoder);
    }

    /**
     * verify that password encoder is used for authentication
     */
    @Test
    public void testPasswordEncoderIsUsedInAuthentication() {
        // make sure security config bean is not using plain text password
        Assert.assertNotEquals("password", securityConfig.getDefaultPassword());

        // authenticate a test user using default credentials for config service
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "password");
        auth = authManager.authenticate(auth);
        Assert.assertNotNull(auth);
        System.out.println(auth.getPrincipal());
    }
}
