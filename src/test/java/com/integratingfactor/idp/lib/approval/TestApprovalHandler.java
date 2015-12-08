package com.integratingfactor.idp.lib.approval;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;

public class TestApprovalHandler implements UserApprovalHandler {

    private String preApprovedClient;

    private boolean inUse = false;

    public void reset() {
        inUse = false;
        preApprovedClient = null;
    }

    public String getPreApprovedClient() {
        return preApprovedClient;
    }

    public void setPreApprovedClient(String preApprovedClient) {
        this.preApprovedClient = preApprovedClient;
    }

    @Override
    public boolean isApproved(AuthorizationRequest authorizationRequest, Authentication userAuthentication) {
        inUse = true;
        return authorizationRequest.isApproved();
    }

    @Override
    public AuthorizationRequest checkForPreApproval(AuthorizationRequest authorizationRequest,
            Authentication userAuthentication) {
        inUse = true;
        authorizationRequest
                .setApproved(preApprovedClient != null && preApprovedClient.equals(authorizationRequest.getClientId()));
        return authorizationRequest;
    }

    @Override
    public AuthorizationRequest updateAfterApproval(AuthorizationRequest authorizationRequest,
            Authentication userAuthentication) {
        inUse = true;
        return authorizationRequest;
    }

    @Override
    public Map<String, Object> getUserApprovalRequest(AuthorizationRequest authorizationRequest,
            Authentication userAuthentication) {
        inUse = true;
        Map<String, Object> model = new HashMap<String, Object>();
        // In case of a redirect we might want the request parameters to be
        // included
        model.putAll(authorizationRequest.getRequestParameters());
        return model;
    }

    public boolean isInUse() {
        return inUse;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

}
