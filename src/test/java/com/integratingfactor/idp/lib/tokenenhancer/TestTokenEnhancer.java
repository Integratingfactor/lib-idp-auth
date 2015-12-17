package com.integratingfactor.idp.lib.tokenenhancer;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

/**
 * a test implementation for token enhancer interface, that enhances access
 * token with one custom attribute
 * 
 * @author gnulib
 *
 */
public class TestTokenEnhancer implements TokenEnhancer {
    public static final String TestCustomAttribute = "test_key";
    public static final String TestCustomValue = UUID.randomUUID().toString();
    public static final int TestExpirySecs = 10;

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        DefaultOAuth2AccessToken result = new DefaultOAuth2AccessToken(accessToken);
        // copy the additional parameters from original token
        Map<String, Object> info = new LinkedHashMap<String, Object>(accessToken.getAdditionalInformation());

        // enhance by adding custom test attributes
        info.put(TestCustomAttribute, TestCustomValue);
        result.setAdditionalInformation(info);

        // also set expiry to custom duration
        result.setExpiration(new Date(new Date().getTime() + TestExpirySecs * 1000));
        return result;
    }

}
