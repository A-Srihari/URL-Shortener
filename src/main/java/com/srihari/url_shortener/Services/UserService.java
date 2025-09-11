package com.srihari.url_shortener.Services;

import com.srihari.url_shortener.Entities.User;
import com.srihari.url_shortener.Models.CreateUserCmd;
import com.srihari.url_shortener.Repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepo userRepository;

    /**
     * Get the currently authenticated user
     * @return User entity or null if not authenticated
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if user is authenticated
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        // Check if it's not an anonymous user
        if ("anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        // Get username from authentication
        String username;
        if (authentication.getPrincipal() instanceof UserDetails) {
            username = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            username = authentication.getName();
        }

        // Find user by username (email in your case)
        Optional<User> userOptional = userRepository.findByEmail(username);
        return userOptional.orElse(null);
    }

    /**
     * Check if current user is authenticated
     * @return true if authenticated, false otherwise
     */
    public boolean isCurrentUserAuthenticated() {
        return getCurrentUser() != null;
    }

    /**
     * Get current user ID safely
     * @return user ID or null if not authenticated
     */
    public Long getCurrentUserId() {
        User currentUser = getCurrentUser();
        return currentUser != null ? currentUser.getId() : null;
    }

    public void createUser(CreateUserCmd cmd) {

    }
}