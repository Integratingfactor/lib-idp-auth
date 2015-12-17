package org.springframework.security.oauth2.provider.token.store;

import java.util.UUID;
import java.util.logging.Logger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

@Configuration
public class TestJWTConfig {
    private static Logger LOG = Logger.getLogger(TestJWTConfig.class.getName());

    public final static String SignKey = UUID.randomUUID().toString();

    @Bean
    public JwtAccessTokenConverter tokenEnhancer() {
        LOG.info("Creating new bean instance JwtAccessTokenConverter");
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey(SignKey);
        return converter;
    }
}
