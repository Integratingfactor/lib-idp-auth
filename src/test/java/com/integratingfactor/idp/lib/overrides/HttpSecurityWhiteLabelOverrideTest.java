package com.integratingfactor.idp.lib.overrides;

import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders;
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
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

import com.integratingfactor.idp.lib.config.SecurityConfig;

@ContextConfiguration(classes = { SecurityConfig.class, TestHttpSecurityWhiteLabelOverrideConfig.class })
@WebAppConfiguration
public class HttpSecurityWhiteLabelOverrideTest extends AbstractTestNGSpringContextTests {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @BeforeMethod
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    public void testHttpLoginPageOverride() throws Exception {
        // clear security context to simulate unauthenticated session
        SecurityContextHolder.clearContext();

        // perform a get request to secured url
        String redirectUrl = this.mockMvc
                .perform(MockMvcRequestBuilders.get(TestHttpSecurityEndpoint.ProtectedResource))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andReturn().getResponse()
                .getRedirectedUrl();
        System.out.println("Login redirection to: " + redirectUrl);
        Assert.assertTrue(redirectUrl.contains(TestHttpSecurityEndpoint.LoginPageUrl));
    }

    /**
     * this test case is using form login from spring security test, however its
     * not using any user/password because spring security test framework
     * injects an anonymous authentication provided and replaces the real
     * provided configured by application's security config bean
     * 
     * @throws Exception
     */
    @Test
    public void testHttpLoginProcessingPageOverride() throws Exception {
        // perform a form login on the custom login processing url
        String redirectUrl = this.mockMvc
                .perform(SecurityMockMvcRequestBuilders.formLogin(TestHttpSecurityEndpoint.LoginProcessingUrl)
                        .userParameter(TestHttpSecurityEndpoint.UsernameParameter)
                        .passwordParam(TestHttpSecurityEndpoint.PasswordParameter))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(SecurityMockMvcResultMatchers.authenticated().withUsername("user")).andReturn().getResponse()
                .getRedirectedUrl();
        System.out.println("Login attempt redirected to: " + redirectUrl);
        Assert.assertTrue(redirectUrl.contains(TestHttpSecurityEndpoint.LoginSuccessUrl));
    }

    @Test
    public void testHttpLogoutUrlOverride() throws Exception {
        // perform a logout on the custom logout url
        String redirectUrl = this.mockMvc
                .perform(SecurityMockMvcRequestBuilders.logout(TestHttpSecurityEndpoint.LogoutUrl))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andReturn().getResponse()
                .getRedirectedUrl();
        System.out.println("Logout attempt redirected to: " + redirectUrl);
        Assert.assertTrue(redirectUrl.contains(TestHttpSecurityEndpoint.LogoutSuccessUrl));

    }
}
