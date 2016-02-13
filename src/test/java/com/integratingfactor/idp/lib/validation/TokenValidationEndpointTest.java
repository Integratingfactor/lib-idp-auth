package com.integratingfactor.idp.lib.validation;

import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.endpoint.AuthorizationEndpoint;
import org.springframework.security.oauth2.provider.endpoint.CheckTokenEndpoint;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpoint;
import org.springframework.security.oauth2.provider.endpoint.WhitelabelApprovalEndpoint;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.integratingfactor.idp.lib.config.OAuth2AuthServerConfig;
import com.integratingfactor.idp.lib.config.SecurityConfig;

@ContextConfiguration(classes = { OAuth2AuthServerConfig.class, SecurityConfig.class })
@WebAppConfiguration
public class TokenValidationEndpointTest extends AbstractTestNGSpringContextTests {
    @Autowired
    AuthorizationEndpoint authEndPoint;

    @Autowired
    private FilterChainProxy springSecurityFilterChain;

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

    private MockMvc mockMvc;
    static String testClientId = "if.test.client";
    static String testClientRedirectUrl = "http://localhost";

    @BeforeMethod
    public void setup() {
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(authEndPoint, tokenEndpoint, checkTokenEndPoint, approvalEndpoint)
                .addFilters(springSecurityFilterChain).build();
    }

    @Test
    public void testTokenValidationIsSecured() throws Exception {

        // perform a validation endpoint request and check if this is secured
        MvcResult validationResult = this.mockMvc
                .perform(MockMvcRequestBuilders.post("/oauth/check_token").param("token", "some bogus token"))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.UNAUTHORIZED.value())).andReturn();
        System.out.println("Got validation response: " + validationResult.getResponse().getContentAsString());
    }

    @Test
    public void testTokenValidationIsAccessibleByClientCredentials() throws Exception {

        // perform a validation endpoint request and check if this is secured
        MvcResult validationResult = this.mockMvc
                .perform(MockMvcRequestBuilders.post("/oauth/check_token").param("token", "some bogus token")
                        .header("Authorization",
                                "Basic " + new String(Base64
                                        .encodeBase64((testClientId + ":").getBytes(Charset.forName("US-ASCII"))))))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.BAD_REQUEST.value())).andReturn();
        System.out.println("Got validation response: " + validationResult.getResponse().getContentAsString());
    }
}
