package com.srihari.url_shortener.Controller;

import com.srihari.url_shortener.Entities.User;
import com.srihari.url_shortener.Repositories.UserRepo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityUtils {

    private final UserRepo userRepository;

    public SecurityUtils(UserRepo userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        String email = authentication.getName();
        System.out.println("Authentication Name: " + authentication.getName());
        System.out.println("Principal: " + authentication.getPrincipal());

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found in DB: " + email));
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }
}