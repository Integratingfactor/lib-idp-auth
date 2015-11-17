package com.integratingfactor.idp.lib.config;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;

@Configuration
@EnableAuthorizationServer
public class OAuth2AuthServerConfig extends AuthorizationServerConfigurerAdapter {
    private static Logger LOG = Logger.getLogger(OAuth2AuthServerConfig.class.getName());

    // get reference to Authentication Manager from Spring Security
    @Autowired
    @Qualifier("authenticationManagerBean") // this is the name of the function
    // that is @Bean annotated from
    // @Configuration bean for spring
    // security configuration
    private AuthenticationManager authenticationManager;

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        LOG.info("enabling /check_token access for all");
        security.checkTokenAccess("permitAll()");
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        LOG.info("Using in-memory clients details service with hard coded end point");
        // use in-memory clients details service with 1 hard coded client
        clients.inMemory()
            .withClient("if.test.client")
                .authorizedGrantTypes("authorization_code")
                .authorities("ROLE_CLIENT")
                .scopes("read")
                .redirectUris("http://localhost:8080")
                .resourceIds("test-resource");
                
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        LOG.info("Using default spring security authentication manager for endpoints security");
        // use default authentication manager from spring security for endpoints security
        endpoints.authenticationManager(authenticationManager);
    }
}
