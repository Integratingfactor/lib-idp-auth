package com.integratingfactor.idp.lib.approval;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.util.OAuth2Utils;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint;
import org.springframework.security.oauth2.provider.endpoint.CheckTokenEndpoint;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.oauth2.provider.endpoint.WhitelabelApprovalEndpoint;
import org.springframework.security.oauth2.provider.token.TokenStore;
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

import com.integratingfactor.idp.lib.clientdetails.TestClientDetailsConfig;
import com.integratingfactor.idp.lib.clientdetails.TestClientDetailsService;
import com.integratingfactor.idp.lib.config.OAuth2AuthServerConfig;
import com.integratingfactor.idp.lib.config.SecurityConfig;
import com.integratingfactor.idp.lib.tokenstore.TestTokenStoreConfig;

@ContextConfiguration(classes = { OAuth2AuthServerConfig.class, SecurityConfig.class, TestApprovalHandlerConfig.class,
        TestClientDetailsConfig.class, TestTokenStoreConfig.class })
@WebAppConfiguration
public class CustomApprovalHandlerTest extends AbstractTestNGSpringContextTests {

    @Autowired
    TestApprovalHandlerConfig config;

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
    TestApprovalHandler approvalHandler;

    @Autowired
    TokenStore tokenStore;

    @Autowired
    TestClientDetailsService clientDetails;

    private MockMvc mockMvc;
    static String testClientId = "if.test.client";
    static String testClientRedirectUrl = "http://localhost";

    @BeforeMethod
    public void setup() {
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(authEndPoint, tokenEndpoint, checkTokenEndPoint, approvalEndpoint).build();
        approvalHandler.reset();
        BaseClientDetails client = new BaseClientDetails();
        client.setClientId(testClientId);
        client.setScope(Arrays.asList("read"));
        client.setAuthorizedGrantTypes(Arrays.asList("authorization_code", "refresh_token"));
        // client.setClientSecret(testClientSecret);
        clientDetails.addClient(client);
    }

    @AfterMethod
    public void reset() {
        clientDetails.removeClient(testClientId);
    }

    private String uriForAuthcodeTokenRequest() {
        return "?client_id=" + testClientId + "&response_type=code&redirect_uri=" + testClientRedirectUrl;
    }

    @Test
    public void testCustomApprovalHandlerIsUsedInAuthEndPoint() throws Exception {
        // send an authorization code request
        this.mockMvc
                .perform(MockMvcRequestBuilders.get("/oauth/authorize" + uriForAuthcodeTokenRequest())
                        .principal(new UsernamePasswordAuthenticationToken("user", "password",
                                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")))))
                .andExpect(MockMvcResultMatchers.status().is2xxSuccessful())
                .andExpect(MockMvcResultMatchers.forwardedUrl("/oauth/confirm_access"));

        // verify that our custom user approval handler was used
        Assert.assertTrue(approvalHandler.isInUse());
    }

    @Test
    public void testCustomApprovalHandlerProvidesPreApprovalInAuthEndPoint() throws Exception {
        // signal out custom approval handler to pass pre approval check
        approvalHandler.setPreApprovedClient(testClientId);

        // send an authorization code request (client has been configured pre
        // approved now)
        String[] params = this.mockMvc
                .perform(MockMvcRequestBuilders.get("/oauth/authorize" + uriForAuthcodeTokenRequest())
                        .principal(new UsernamePasswordAuthenticationToken("user", "password",
                                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")))))
                // verify that we did not get redirected to confirm access
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andReturn().getResponse()
                .getRedirectedUrl().split("=");

        // verify that we got a pre-approved authorization code
        Assert.assertTrue(params[0].contains("code"));
        System.out.println("Got pre approved token: " + params[1]);

        // verify that our custom user approval handler was used
        Assert.assertTrue(approvalHandler.isInUse());
    }

    @Test
    public void testFullTokenGrantWithCustomApprovalHandler() throws Exception {
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
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/oauth/token").param(OAuth2Utils.CLIENT_ID, testClientId)
                        .param(OAuth2Utils.GRANT_TYPE, "authorization_code").param("code", params[1])
                        .principal(new UsernamePasswordAuthenticationToken(testClientId, "",
                                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")))))
                // we expect a 200 success
                .andExpect(MockMvcResultMatchers.status().is(200));

        // verify that our custom user approval handler was used
        Assert.assertTrue(approvalHandler.isInUse());
    }
}
