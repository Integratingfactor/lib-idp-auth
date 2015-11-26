package com.integratingfactor.idp.lib.clientdetails;

import java.util.logging.Logger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestClientDetailsConfig {
    private static Logger LOG = Logger.getLogger(TestClientDetailsConfig.class.getName());

    @Bean
    public TestClientDetailsService clientDetailsService() {
        LOG.info("Creating new bean instance TestClientDetailsService");
        return new TestClientDetailsService();
    }
}
