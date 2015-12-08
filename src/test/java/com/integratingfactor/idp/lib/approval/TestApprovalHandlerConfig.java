package com.integratingfactor.idp.lib.approval;

import java.util.logging.Logger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestApprovalHandlerConfig {
    private static Logger LOG = Logger.getLogger(TestApprovalHandlerConfig.class.getName());

    /**
     * need to initialize custom approval handler with the necessary
     * dependencies, since UserApprovalHandler interface itself does not have
     * any methods for initialization and hence OAuth2 configurer cannot
     * initialize the bean (has no clue what type it is and what are the
     * dependencies)
     * 
     * @return
     */
    @Bean
    TestApprovalHandler userApprovalHandler() {
        LOG.info("Creating new bean instance TestApprovalHandler");
        return new TestApprovalHandler();
    }
}
