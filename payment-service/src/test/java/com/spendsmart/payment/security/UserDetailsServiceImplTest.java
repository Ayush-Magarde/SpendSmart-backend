package com.spendsmart.payment.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;

class UserDetailsServiceImplTest {

    private final UserDetailsServiceImpl userDetailsService = new UserDetailsServiceImpl();

    @Test
    void loadUserByUsername_returnsUserDetailsWithCorrectUsername() {
        UserDetails userDetails = userDetailsService.loadUserByUsername("test@example.com");

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("test@example.com");
    }

    @Test
    void loadUserByUsername_returnsEmptyPassword() {
        UserDetails userDetails = userDetailsService.loadUserByUsername("user@test.com");

        assertThat(userDetails.getPassword()).isEmpty();
    }

    @Test
    void loadUserByUsername_returnsRoleUserAuthority() {
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin@test.com");

        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority())
                .isEqualTo("ROLE_USER");
    }

    @Test
    void loadUserByUsername_isEnabledAndNotExpired() {
        UserDetails userDetails = userDetailsService.loadUserByUsername("user@test.com");

        assertThat(userDetails.isEnabled()).isTrue();
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isAccountNonLocked()).isTrue();
        assertThat(userDetails.isCredentialsNonExpired()).isTrue();
    }
}
