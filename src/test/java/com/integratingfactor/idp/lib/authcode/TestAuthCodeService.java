package com.integratingfactor.idp.lib.authcode;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;

public class TestAuthCodeService implements AuthorizationCodeServices {
    private static final Map<String, OAuth2Authentication> inMemMap;

    static {
        inMemMap = new HashMap<String, OAuth2Authentication>();
    }

    /**
     * store the incoming authentication by associating it with a random code string
     * @param authentication incoming token request
     * @return code string associated with incoming request
     */
    @Override
    public String createAuthorizationCode(OAuth2Authentication authentication) {
        String code = UUID.randomUUID().toString();
        inMemMap.put(code, authentication);
        return code;
    }

    /**
     * return back the authentication saved earlier associated with the provided authorization code
     * @param code authorization code issued earlier
     * @return authentication associated with the authorization code
     * @throws InvalidGrantException if no authentication found for the authorization code
     */
    @Override
    public OAuth2Authentication consumeAuthorizationCode(String code) throws InvalidGrantException {
        OAuth2Authentication auth = inMemMap.remove(code);
        if (auth == null)
            throw new InvalidGrantException("No authorization found: " + code);
        return auth;
    }

}
