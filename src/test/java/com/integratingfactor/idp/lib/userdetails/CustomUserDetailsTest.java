package com.integratingfactor.idp.lib.userdetails;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.integratingfactor.idp.lib.config.SecurityConfig;

@ContextConfiguration(classes = { SecurityConfig.class, TestUserDetailsConfig.class })
@WebAppConfiguration
public class CustomUserDetailsTest extends AbstractTestNGSpringContextTests {

    @Autowired
    SecurityConfig securityConfig;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    TestUserDetailsServiceImpl testUsers;

    static String testUsername = "TestUser";
    static String testPassword = "Secret!!@#$%#$%";

    @BeforeMethod
    public void setup() {
        TestUserDetailsImpl user = new TestUserDetailsImpl();
        user.setUsername(testUsername);
        user.setPassword(passwordEncoder.encode(testPassword));
        testUsers.addUser(user);
    }

    @AfterMethod
    public void reset() {
        testUsers.removeUser(testUsername);
    }

    @Test
    public void securityConfigurationLoads() {
        Assert.assertNotNull(authManager);
        Assert.assertNotNull(springSecurityFilterChain);
    }

    /**
     * verify that user details are retrieved from custom user details service
     */
    @Test
    public void testCustomUserDetailsServiceIsUsedInAuthentication() {
        // authenticate a test user using default credentials for config service
        Authentication auth = new UsernamePasswordAuthenticationToken(testUsername, testPassword);
        auth = authManager.authenticate(auth);
        Assert.assertNotNull(auth);
        System.out.println(auth.getPrincipal());
    }
}
