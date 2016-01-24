package com.integratingfactor.idp.lib.overrides;

import java.util.logging.Logger;

import org.springframework.context.annotation.Bean;
import org.springframework.test.context.web.WebAppConfiguration;

@WebAppConfiguration
public class TestHttpSecurityWhiteLabelOverrideConfig {
    private static Logger LOG = Logger.getLogger(TestHttpSecurityWhiteLabelOverrideConfig.class.getName());

    @Bean
    public TestHttpSecurityEndpoint httpSecurityWhiteLabelOverride() {
        LOG.info("Creating new bean instance TestHttpSecurityEndpoint");
        return new TestHttpSecurityEndpoint();
    }

    @Bean
    public TestCustomAuthenticationFilter customAuthenticationFilter() {
        LOG.info("Creating new bean instance TestCustomAuthenticationFilter");
        return new TestCustomAuthenticationFilter();
    }
}
