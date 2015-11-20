package com.integratingfactor.idp.lib.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;

@ContextConfiguration(classes={OAuth2AuthServerConfig.class, SecurityConfig.class})
@WebAppConfiguration
public class OAuth2AuthServerConfigTest extends AbstractTestNGSpringContextTests {   
    @Autowired
    AuthorizationEndpoint authEndPoint;

    @Test
    public void securityConfigurationLoads() {
        Assert.assertNotNull(authEndPoint);
    }

}
