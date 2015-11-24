package com.integratingfactor.idp.lib.userdetails;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class TestUserDetailsServiceImpl implements UserDetailsService {
    private static final Map<String, UserDetails> inMemMap;

    static {
        inMemMap = new HashMap<String, UserDetails>();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails user = inMemMap.get(username);
        if (user == null)
            throw new UsernameNotFoundException("No user found with username: " + username);
        return user;
    }

    public void addUser(TestUserDetailsImpl userDetails) {
        if (userDetails == null)
            return;
        inMemMap.put(userDetails.getUsername(), userDetails);
    }

    public void removeUser(String username) {
        inMemMap.remove(username);
    }
}
