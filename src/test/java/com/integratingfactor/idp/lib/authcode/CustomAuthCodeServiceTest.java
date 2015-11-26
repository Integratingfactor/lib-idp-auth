package com.integratingfactor.idp.lib.authcode;

import java.util.Arrays;

import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint;
import org.springframework.security.oauth2.provider.endpoint.CheckTokenEndpoint;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.oauth2.provider.endpoint.WhitelabelApprovalEndpoint;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.integratingfactor.idp.lib.config.OAuth2AuthServerConfig;
import com.integratingfactor.idp.lib.config.SecurityConfig;

@ContextConfiguration(classes = { OAuth2AuthServerConfig.class, SecurityConfig.class, TestAuthCodeConfig.class })
@WebAppConfiguration
public class CustomAuthCodeServiceTest extends AbstractTestNGSpringContextTests {

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
    TestAuthCodeService testAuthCodeService;

    private MockMvc mockMvc;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(authEndPoint, tokenEndpoint, checkTokenEndPoint, approvalEndpoint).build();
    }

    private String uriForAuthcodeTokenRequest() {
        return "?client_id=if.test.client&response_type=code&redirect_uri=http://localhost";
    }

    @Test
    public void testAccessTokenGrantWithValidAuthCode() throws Exception {
        // get a valid authorization code from /oauth/authorize endpoint
        String[] params = this.mockMvc
                .perform(MockMvcRequestBuilders.post("/oauth/authorize")
                        .principal(new UsernamePasswordAuthenticationToken("user", "password",
                                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))))
                .param(OAuth2Utils.USER_OAUTH_APPROVAL, uriForAuthcodeTokenRequest())
                .sessionAttr("authorizationRequest", new AuthorizationRequest(null, null, "if.test.client", null, null,
                        null, true, null, "http://localhost", null)))
                .andReturn().getResponse().getRedirectedUrl().split("=");
        System.out.println("Got authorization code: " + params[1]);

        // perform POST to /oauth/token endpoint with authentication and correct
        // auth code
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/oauth/token").param(OAuth2Utils.CLIENT_ID, "if.test.client")
                        .param(OAuth2Utils.GRANT_TYPE, "authorization_code").param("code", params[1])
                        .principal(new UsernamePasswordAuthenticationToken("if.test.client", "",
                                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")))))
                // we expect a 200 success
                .andExpect(MockMvcResultMatchers.status().is(200));
    }

    @Test
    public void testCustomAuthorizationCodeServiceIsUsedByAuthorizationEndpoint() throws Exception {
        // get a valid authorization code from /oauth/authorize endpoint
        String[] params = this.mockMvc
                .perform(MockMvcRequestBuilders.post("/oauth/authorize")
                        .principal(new UsernamePasswordAuthenticationToken("user", "password",
                                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))))
                .param(OAuth2Utils.USER_OAUTH_APPROVAL, uriForAuthcodeTokenRequest())
                .sessionAttr("authorizationRequest", new AuthorizationRequest(null, null, "if.test.client", null, null,
                        null, true, null, "http://localhost", null)))
                .andReturn().getResponse().getRedirectedUrl().split("=");
        System.out.println("Got authorization code: " + params[1]);

        // verify that our custom service has the code
        Assert.assertNotNull(testAuthCodeService.consumeAuthorizationCode(params[1]));
    }

    @Test
    public void testCustomAuthorizationCodeServiceIsUsedByTokenEndpoint() throws Exception {
        // get a valid authorization code from /oauth/authorize endpoint
        String[] params = this.mockMvc
                .perform(MockMvcRequestBuilders.post("/oauth/authorize")
                        .principal(new UsernamePasswordAuthenticationToken("user", "password",
                                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))))
                .param(OAuth2Utils.USER_OAUTH_APPROVAL, uriForAuthcodeTokenRequest())
                .sessionAttr("authorizationRequest", new AuthorizationRequest(null, null, "if.test.client", null, null,
                        null, true, null, "http://localhost", null)))
                .andReturn().getResponse().getRedirectedUrl().split("=");
        System.out.println("Got authorization code: " + params[1]);

        // insert a new auth code into our custom service
        String customCode = testAuthCodeService
                .createAuthorizationCode(testAuthCodeService.consumeAuthorizationCode(params[1]));
        System.out.println("Using new authorization code: " + customCode);

        // perform POST to /oauth/token endpoint with authentication and new
        // auth code
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/oauth/token").param(OAuth2Utils.CLIENT_ID, "if.test.client")
                        .param(OAuth2Utils.GRANT_TYPE, "authorization_code").param("code", customCode)
                        .principal(new UsernamePasswordAuthenticationToken("if.test.client", "",
                                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")))))
                // we expect a 200 success
                .andExpect(MockMvcResultMatchers.status().is(200));
    }
}