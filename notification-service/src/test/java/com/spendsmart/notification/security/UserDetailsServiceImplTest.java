package com.spendsmart.notification.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

class UserDetailsServiceImplTest {

    private final UserDetailsServiceImpl userDetailsService = new UserDetailsServiceImpl();

    @Test
    void loadUserByUsername_ReturnsUserDetails() {
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@test.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("test@test.com");
        assertThat(userDetails.getPassword()).isEmpty();
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }
}
