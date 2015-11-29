package com.integratingfactor.idp.lib.tokenstore;

import java.util.Arrays;

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
import org.springframework.security.oauth2.provider.endpoint.WhitelabelApprovalEndpoint;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
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

@ContextConfiguration(classes = { OAuth2AuthServerConfig.class, SecurityConfig.class, TestTokenStoreConfig.class })
@WebAppConfiguration
public class CustomTokenStoreTest extends AbstractTestNGSpringContextTests {
    @Autowired
    AuthorizationEndpoint authEndPoint;

    @Autowired
    TokenEndpoint tokenEndpoint;

    @Autowired
    CheckTokenEndpoint checkTokenEndPoint;

    @Autowired
    WhitelabelApprovalEndpoint approvalEndpoint;

    @Autowired
    SecurityConfig securityConfig;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    TokenStore tokenStore;

    private MockMvc mockMvc;
    static String testClientId = "if.test.client";
    static String testClientRedirectUrl = "http://localhost";

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(authEndPoint, tokenEndpoint, checkTokenEndPoint, approvalEndpoint).build();
        InMemoryTokenStore inMemStore = (InMemoryTokenStore)tokenStore;
        inMemStore.clear();
    }

    private String uriForAuthcodeTokenRequest() {
        return "?client_id=" + testClientId + "&response_type=code&redirect_uri=" + testClientRedirectUrl;
    }

    @Test
    public void testCustomTokenServiceIsUsed() throws Exception {
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
        
        // verify that our custom token store was used and populated
        Assert.assertEquals(((OAuth2AccessToken)tokenStore.findTokensByClientId(testClientId).toArray()[0]).getValue(),  token.getValue());
    }
}
