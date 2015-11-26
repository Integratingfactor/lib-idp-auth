package com.integratingfactor.idp.lib.clientdetails;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;

public class TestClientDetailsService implements ClientDetailsService {
    private static final Map<String, ClientDetails> inMemMap;

    static {
        inMemMap = new HashMap<String, ClientDetails>();
    }

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        ClientDetails client = inMemMap.get(clientId);
        if (client == null)
            throw new ClientRegistrationException("No client found with clientId: " + clientId);
        return client;
    }

    public void addClient(ClientDetails clientDetails) {
        if (clientDetails == null)
            return;
        inMemMap.put(clientDetails.getClientId(), clientDetails);
    }

    public void removeClient(String clientId) {
        inMemMap.remove(clientId);
    }
}
