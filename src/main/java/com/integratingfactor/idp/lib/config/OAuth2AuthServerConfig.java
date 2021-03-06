package com.integratingfactor.idp.lib.config;

import java.util.logging.Logger;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

@Configuration
@EnableAuthorizationServer
public class OAuth2AuthServerConfig extends AuthorizationServerConfigurerAdapter implements ApplicationContextAware {
    private static Logger LOG = Logger.getLogger(OAuth2AuthServerConfig.class.getName());

    // get reference to Authentication Manager from Spring Security
    @Autowired
    @Qualifier("authenticationManagerBean") // this is the name of the function
    // that is @Bean annotated from
    // @Configuration bean for spring
    // security configuration
    private AuthenticationManager authenticationManager;

    AuthorizationCodeServices authCodeService;
    
    TokenStore tokenStore;

    UserApprovalHandler approvalHandler;

    TokenEnhancer tokenEnhancer;

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        if (tokenEnhancer != null && tokenEnhancer instanceof JwtAccessTokenConverter) {
            LOG.info("JWT detected -- enabling /token_key access for all");
            security.tokenKeyAccess("permitAll()");
        }
        LOG.info("securing token validation endpoint for full authentication");
        security.checkTokenAccess("isFullyAuthenticated()");
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        LOG.info("adding a default in-memory clients details service with hard coded end point");
        // use in-memory clients details service with 1 hard coded client
        // however, this will get overridden by any ClientDetailsService bean
        // present in the application context
        clients.inMemory().withClient("if.test.client").authorizedGrantTypes("authorization_code")
                .authorities("ROLE_CLIENT").scopes("read").resourceIds("test-resource");

    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        LOG.info("Using default spring security authentication manager for endpoints security");
        // use default authentication manager from spring security for endpoints
        // security
        endpoints.authenticationManager(authenticationManager);

        // use application's auth code service if provided
        if (authCodeService != null) {
            endpoints.authorizationCodeServices(authCodeService);
        }

		// use application's token store if provided
		if (tokenStore != null) {
			endpoints.tokenStore(tokenStore);
		}

        // use application's user approval handler if provided
        if (approvalHandler != null) {
            endpoints.userApprovalHandler(approvalHandler);
        }

        // use application's token enhancer if provided
        if (tokenEnhancer != null) {
            endpoints.tokenEnhancer(tokenEnhancer);
            // seems like there is bug, if token enhancer is specified, then
            // token converter need to be specified explicitly
            LOG.info("Adding DefaultAccessTokenConverter into endpoints configuration");
            endpoints.accessTokenConverter(new DefaultAccessTokenConverter());
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        authCodeService = null;
        try {
            authCodeService = ctx.getBean(AuthorizationCodeServices.class);
            LOG.info("Using custom application provided auth code service: " + authCodeService.toString());
        } catch (BeansException e) {
            LOG.info("No custom application provided auth code service: " + e.getMessage());
        } catch (Exception e) {
            LOG.info("Caught exception: " + e.getMessage());
        }

		tokenStore = null;
		try {
			tokenStore = ctx.getBean(TokenStore.class);
			LOG.info("Using custom application provided token store: " + tokenStore.toString());
		} catch (BeansException e) {
			LOG.info("No custom application provided token store: " + e.getMessage());
		} catch (Exception e) {
			LOG.info("Caught exception: " + e.getMessage());
		}

        approvalHandler = null;
        try {
            approvalHandler = ctx.getBean(UserApprovalHandler.class);
            LOG.info("Using custom application provided user approval handler: " + approvalHandler.toString());
        } catch (BeansException e) {
            LOG.info("No custom application provided user approval handler: " + e.getMessage());
        } catch (Exception e) {
            LOG.info("Caught exception: " + e.getMessage());
        }

        tokenEnhancer = null;
        try {
            tokenEnhancer = ctx.getBean(TokenEnhancer.class);
            LOG.info("Using custom application provided token enhancer: " + tokenEnhancer.toString());
        } catch (BeansException e) {
            LOG.info("No custom application provided token enhancer: " + e.getMessage());
        } catch (Exception e) {
            LOG.info("Caught exception: " + e.getMessage());
        }
    }
}
