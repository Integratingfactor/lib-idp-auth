package com.integratingfactor.idp.lib.tokenenhancer;

import java.util.logging.Logger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestTokenEnhancerConfig {
    private static Logger LOG = Logger.getLogger(TestTokenEnhancerConfig.class.getName());

    @Bean
    public TestTokenEnhancer tokenEnhancer() {
        LOG.info("Creating new bean instance TestTokenEnhancer");
        return new TestTokenEnhancer();
    }
}
