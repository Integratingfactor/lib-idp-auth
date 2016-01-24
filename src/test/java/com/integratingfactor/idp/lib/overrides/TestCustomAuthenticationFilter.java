package com.integratingfactor.idp.lib.overrides;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

public class TestCustomAuthenticationFilter extends AbstractAuthenticationProcessingFilter
        implements IdpAuthenticationFilter {

    @Autowired
    AuthenticationManager authManager;

    boolean used = false;

    public static final String TestAuthenticationFilterUrl = "/test/authentication";

    public void reset() {
        used = false;
    }

    public boolean isUsed() {
        return used;
    }

    public TestCustomAuthenticationFilter() {
        super(new AntPathRequestMatcher(TestAuthenticationFilterUrl, "POST"));
    }

    protected TestCustomAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);
        // TODO Auto-generated constructor stub
        reset();
    }

    @Override
    public void afterPropertiesSet() {
        super.setAuthenticationManager(authManager);
    }

    @Override
    public String getFilterUrl() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Class<? extends Filter> getNextFilter() {
        return UsernamePasswordAuthenticationFilter.class;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        used = true;
        return null;
    }

}
