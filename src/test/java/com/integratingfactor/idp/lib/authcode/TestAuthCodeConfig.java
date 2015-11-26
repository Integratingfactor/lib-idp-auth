package com.integratingfactor.idp.lib.authcode;

import java.util.logging.Logger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;

@Configuration
public class TestAuthCodeConfig {
    private static Logger LOG = Logger.getLogger(TestAuthCodeConfig.class.getName());

    @Bean
    public AuthorizationCodeServices authorizationCodeServices() {
        LOG.info("Creating new bean instance TestAuthCodeService");
        return new TestAuthCodeService();
    }
}
