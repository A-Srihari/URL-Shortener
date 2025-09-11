package com.srihari.url_shortener.Services;

import com.srihari.url_shortener.Entities.User;
import com.srihari.url_shortener.Models.CreateUserCmd;
import com.srihari.url_shortener.Repositories.UserRepo;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepo userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

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

    /**
     * Create a new user
     * @param cmd CreateUserCmd containing user information
     * @return Created User entity
     */
    public User createUser(CreateUserCmd cmd) {
        // Check if user already exists
        if (userRepository.existsByEmail(cmd.email())) {
            throw new IllegalArgumentException("User with email " + cmd.email() + " already exists");
        }

        // Create new user
        User user = new User();
        user.setEmail(cmd.email());
        user.setPassword(passwordEncoder.encode(cmd.password()));
        user.setUsername(cmd.name());

        return userRepository.save(user);
    }

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @PostConstruct
    public void printDbUrl() {
        System.out.println("DB URL from env: " + dbUrl);
    }

}