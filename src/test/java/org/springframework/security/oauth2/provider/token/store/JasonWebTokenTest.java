package org.springframework.security.oauth2.provider.token.store;

import java.util.Arrays;
import java.util.Map;

import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint;
import org.springframework.security.oauth2.provider.endpoint.CheckTokenEndpoint;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.oauth2.provider.endpoint.TokenKeyEndpoint;
import org.springframework.security.oauth2.provider.endpoint.WhitelabelApprovalEndpoint;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.integratingfactor.idp.lib.config.OAuth2AuthServerConfig;
import com.integratingfactor.idp.lib.config.SecurityConfig;

@ContextConfiguration(classes = { OAuth2AuthServerConfig.class, SecurityConfig.class, TestJWTConfig.class })
@WebAppConfiguration
public class JasonWebTokenTest extends AbstractTestNGSpringContextTests {
    @Autowired
    AuthorizationEndpoint authEndPoint;

    @Autowired
    TokenEndpoint tokenEndpoint;

    @Autowired
    CheckTokenEndpoint checkTokenEndPoint;

    @Autowired
    WhitelabelApprovalEndpoint approvalEndpoint;

    @Autowired
    TokenKeyEndpoint tokenKey;

    @Autowired
    SecurityConfig securityConfig;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtAccessTokenConverter jwt;

    private MockMvc mockMvc;
    static String testClientId = "if.test.client";
    static String testClientRedirectUrl = "http://localhost";

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(authEndPoint, tokenEndpoint, checkTokenEndPoint, approvalEndpoint, tokenKey).build();
    }

    private String uriForAuthcodeTokenRequest() {
        return "?client_id=" + testClientId + "&response_type=code&redirect_uri=" + testClientRedirectUrl;
    }

    @Test
    public void testJwtIsUsedInTokenGrantEndPoint() throws Exception {
        // get a valid authorization code from /oauth/authorize endpoint
        String[] params = this.mockMvc
                .perform(MockMvcRequestBuilders.post("/oauth/authorize")
                        .principal(new UsernamePasswordAuthenticationToken("user", "password",
                                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))))
                .param(OAuth2Utils.USER_OAUTH_APPROVAL, uriForAuthcodeTokenRequest())
                .sessionAttr("authorizationRequest", new AuthorizationRequest(null, null, testClientId, null, null,
                        null, true, null, testClientRedirectUrl, null)))
                .andReturn().getResponse().getRedirectedUrl().split("=");
        System.out.println("Got authorization code: " + params[1]);

        // perform POST to /oauth/token endpoint with authentication and correct
        // auth code
        ResultActions result = this.mockMvc
                .perform(MockMvcRequestBuilders.post("/oauth/token").param(OAuth2Utils.CLIENT_ID, testClientId)
                        .param(OAuth2Utils.GRANT_TYPE, "authorization_code").param("code", params[1])
                        .principal(new UsernamePasswordAuthenticationToken(testClientId, "",
                                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")))))
                // we expect a 200 success
                .andExpect(MockMvcResultMatchers.status().is(200));
        String response = result.andReturn().getResponse().getContentAsString();
        System.out.println("Got response: " + response);
        OAuth2AccessToken token = new ObjectMapper().readValue(response, OAuth2AccessToken.class);

        // verify that token is of type JWT
        Assert.assertNotNull(token.getAdditionalInformation().get(JwtAccessTokenConverter.TOKEN_ID));

        // verify that token value can be decoded correctly
        Map<String, Object> decodedToken = jwt.decode(token.getValue());
        System.out.println("Decoded Token is: " + new ObjectMapper().writeValueAsString(decodedToken));
        Assert.assertEquals(decodedToken.get(AccessTokenConverter.CLIENT_ID), testClientId);
    }

    @Test
    public void testJwtIsUsedInTokenKeyEndPoint() throws Exception {
        // perform GET to /oauth/check_token endpoint with client
        // authentication
        ResultActions result = this.mockMvc
                .perform(MockMvcRequestBuilders.get("/oauth/token_key")
                        .principal(new UsernamePasswordAuthenticationToken(testClientId, "",
                                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")))))
                // we expect a 200 success
                .andExpect(MockMvcResultMatchers.status().is(200));
        Map<?, ?> tokenKey = new ObjectMapper().readValue(result.andReturn().getResponse().getContentAsString(),
                Map.class);
        System.out.println("Got response: " + result.andReturn().getResponse().getContentAsString());
        Assert.assertEquals(tokenKey.get("value"), TestJWTConfig.SignKey);
    }

    // TODO: figure out how to test this, currently security policy is not
    // applying and hence all access are enabled in unit tests
    // @Test
    public void testCheckTokenEndPointIsDisabled() throws Exception {
        // perform POST to /oauth/check_token endpoint with authentication
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/oauth/check_token").param("token", "some random value")
                        .principal(new UsernamePasswordAuthenticationToken(testClientId, "",
                                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")))))
                // we expect a 403 not authorized
                .andExpect(MockMvcResultMatchers.status().is(403));
    }
}
