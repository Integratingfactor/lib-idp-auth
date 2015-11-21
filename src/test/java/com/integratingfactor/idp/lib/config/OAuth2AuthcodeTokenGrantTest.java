package com.integratingfactor.idp.lib.config;

import java.util.Arrays;

import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
import org.springframework.web.context.WebApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ContextConfiguration(classes = { OAuth2AuthServerConfig.class, SecurityConfig.class })
@WebAppConfiguration
public class OAuth2AuthcodeTokenGrantTest extends AbstractTestNGSpringContextTests {
    @Autowired
    AuthorizationEndpoint authEndPoint;

    @Autowired
    TokenEndpoint tokenEndpoint;

    @Autowired
    CheckTokenEndpoint checkTokenEndPoint;

    @Autowired
    WhitelabelApprovalEndpoint approvalEndpoint;

    @Autowired
    WebApplicationContext wac;

    @Autowired
    MockHttpServletRequest request;

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
    public void testAuthcodeTokenGrantBeforeAuthentication() throws Exception {
        // clear security context to simulate unauthenticated session
        SecurityContextHolder.clearContext();
        try {
            this.mockMvc.perform(MockMvcRequestBuilders.get("/oauth/authorize" + uriForAuthcodeTokenRequest()));
            Assert.fail("Did not authenticate user");
        } catch (Exception e) {
            Assert.assertTrue(e.getCause() instanceof InsufficientAuthenticationException);
            System.out.println("Successfully challenged authentication:" + e.getCause().getMessage());
        }
    }

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
                .perform(MockMvcRequestBuilders.get("/oauth/authorize" + uriForAuthcodeTokenRequest()).principal(auth))
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
        AuthorizationRequest authorization = new AuthorizationRequest(null, null, "if.test.client", null, null, null,
                true, null, "http://localhost", null);

        // perform POST to /oauth/authorize endpoint from approval page HTTP
        // form by pre-populating user authentication to simulate successful
        // login and user authorization in the session context to simulate user
        // approval
        this.mockMvc
                .perform(MockMvcRequestBuilders.post("/oauth/authorize").principal(auth)
                        .param(OAuth2Utils.USER_OAUTH_APPROVAL, uriForAuthcodeTokenRequest())
                        .sessionAttr("authorizationRequest", authorization))
                // we expect a redirect to client's url with authorization code as parameter
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("http://localhost?code=*"));

    }
}
