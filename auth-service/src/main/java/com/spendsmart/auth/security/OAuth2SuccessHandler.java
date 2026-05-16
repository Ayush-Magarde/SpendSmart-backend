package com.spendsmart.auth.security;

import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@lombok.extern.slf4j.Slf4j
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @org.springframework.beans.factory.annotation.Value("${frontend.url}")
    private String frontendUrl;

    @org.springframework.beans.factory.annotation.Value("${admin.email}")
    private String adminEmail;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        log.info("OAuth2 Success! User: {}, Email: {}", name, email);

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName(name);
            
            if (email != null && email.equalsIgnoreCase(adminEmail)) {
                user.setRole("ADMIN");
            } else {
                user.setRole("USER");
            }
            
            user.setProvider("GOOGLE");
            user.setPassword("");
            user = userRepository.save(user); // ✅ ensure saved entity is returned
        }

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole(),
                user.getId()
        );

        // ✅ FIXED: Configurable redirect URL
        response.sendRedirect(frontendUrl + "/oauth-success?token=" + token);
    }
}
