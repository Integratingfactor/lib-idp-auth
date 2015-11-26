package com.integratingfactor.idp.lib.clientdetails;

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
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.integratingfactor.idp.lib.config.OAuth2AuthServerConfig;
import com.integratingfactor.idp.lib.config.SecurityConfig;

@ContextConfiguration(classes = { OAuth2AuthServerConfig.class, SecurityConfig.class, TestClientDetailsConfig.class })
@WebAppConfiguration
public class CustomClientDetailsTest extends AbstractTestNGSpringContextTests {

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
    TestClientDetailsService testClients;

    static String testClientId = "test.client";
    static String testClientRedirectUrl = "http://mock.test.url";
    static String testClientSecret = "Secret!!@#$%#$%";

    private MockMvc mockMvc;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(authEndPoint, tokenEndpoint, checkTokenEndPoint, approvalEndpoint).build();
        BaseClientDetails client = new BaseClientDetails();
        client.setClientId(testClientId);
        client.setScope(Arrays.asList("read"));
        client.setAuthorizedGrantTypes(Arrays.asList("authorization_code"));
        // client.setClientSecret(testClientSecret);
        testClients.addClient(client);
    }

    @AfterMethod
    public void reset() {
        testClients.removeClient(testClientId);
    }

    private String uriForAuthcodeTokenRequestByCustomClient() {
        return "?client_id=" + testClientId + "&response_type=code&redirect_uri=" + testClientRedirectUrl;
    }

    @Test
    public void securityConfigurationLoads() {
        Assert.assertNotNull(authEndPoint);
        Assert.assertNotNull(tokenEndpoint);
    }

    /**
     * test with custom test client instead of default client and make sure it
     * works
     * 
     * @throws Exception
     */

    @Test
    public void testAuthcodeTokenGrantAfterAuthentication() throws Exception {
        // build an authenticated user detail, to simulate authenticated session
        GrantedAuthority authorities = new SimpleGrantedAuthority("ROLE_USER");
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "password", Arrays.asList(authorities));
        SecurityContextHolder.getContext().setAuthentication(auth);

        // perform GET to /oauth/authorize endpoint for authorization code token
        // request by pre-populating user authentication to simulate successful
        // login
        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/oauth/authorize" + uriForAuthcodeTokenRequestByCustomClient())
                        .principal(auth))
                // we expect status 200 OK with forwarded URL to approval page
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.forwardedUrl("/oauth/confirm_access"));
    }

    @Test
    public void testAuthcodeTokenGrantAfterApproval() throws Exception {
        // build an authenticated user detail, to simulate authenticated session
        GrantedAuthority authorities = new SimpleGrantedAuthority("ROLE_USER");
        Authentication auth = new UsernamePasswordAuthenticationToken("user", "password", Arrays.asList(authorities));
        SecurityContextHolder.getContext().setAuthentication(auth);

        // build a user authorization to simulate user approval on approval page
        AuthorizationRequest authorization = new AuthorizationRequest(null, null, testClientId, null, null, null, true,
                null, testClientRedirectUrl, null);

        // perform POST to /oauth/authorize endpoint from approval page HTTP
        // form by pre-populating user authentication to simulate successful
        // login and user authorization in the session context to simulate user
        // approval
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/oauth/authorize").principal(auth)
                        .param(OAuth2Utils.USER_OAUTH_APPROVAL, uriForAuthcodeTokenRequestByCustomClient())
                        .sessionAttr("authorizationRequest", authorization))
                // we expect a redirect to client's url with authorization code
                // as parameter
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern(testClientRedirectUrl + "?code=*"));

    }

    @Test
    public void testAccessTokenGrantWithValidAuthCode() throws Exception {
        // get a valid authorization code from /oauth/authorize endpoint
        String[] params = this.mockMvc
                .perform(MockMvcRequestBuilders.post("/oauth/authorize")
                        .principal(new UsernamePasswordAuthenticationToken("user", "password",
                                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))))
                .param(OAuth2Utils.USER_OAUTH_APPROVAL, uriForAuthcodeTokenRequestByCustomClient())
                .sessionAttr("authorizationRequest", new AuthorizationRequest(null, null, testClientId, null, null,
                        null, true, null, testClientRedirectUrl, null)))
                .andReturn().getResponse().getRedirectedUrl().split("=");
        System.out.println("Got authorization code: " + params[1]);

        // perform POST to /oauth/token endpoint with authentication and correct
        // auth code
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/oauth/token").param(OAuth2Utils.CLIENT_ID, testClientId)
                        .param(OAuth2Utils.GRANT_TYPE, "authorization_code").param("code", params[1])
                        .principal(new UsernamePasswordAuthenticationToken(testClientId, testClientSecret,
                                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")))))
                // we expect a 400 error
                .andExpect(MockMvcResultMatchers.status().is(200));
    }
}
