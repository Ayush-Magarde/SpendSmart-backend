package com.spendsmart.expense.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username: {}", username);
        
        // Since this is a microservice architecture, we'll create a simple user
        // In a real implementation, you might call the auth-service to validate the user
        return new User(
            username,
            "", // password not needed for JWT validation
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
