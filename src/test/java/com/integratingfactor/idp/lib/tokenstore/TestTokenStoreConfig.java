package com.integratingfactor.idp.lib.tokenstore;

import java.util.logging.Logger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

@Configuration
public class TestTokenStoreConfig {
	private static Logger LOG = Logger.getLogger(TestTokenStoreConfig.class.getName());

	@Bean
	public TokenStore tokenStoreService() {
		LOG.info("Creating new bean instance InMemoryTokenStore");
		return new InMemoryTokenStore();
	}
}
