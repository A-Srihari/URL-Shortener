package com.srihari.url_shortener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NullPointerException.class)
    public String handleNullPointer(NullPointerException e, Model model) {
        log.error("NullPointerException: {}", e.getMessage(), e);

        // Check if it's related to user authentication
        if (e.getMessage() != null && e.getMessage().contains("currentUser")) {
            model.addAttribute("errorMessage", "Please log in to access this feature");
            return "redirect:/login?error=authentication_required";
        }

        model.addAttribute("errorMessage", "An unexpected error occurred");
        return "error/404";
    }

    @ExceptionHandler(AuthenticationException.class)
    public String handleAuthenticationException(AuthenticationException e, Model model) {
        log.error("Authentication error: {}", e.getMessage());
        return "redirect:/login?error=authentication_failed";
    }

    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception e, Model model) {
        log.error("Unexpected exception: {}", e.getMessage(), e);
        model.addAttribute("errorMessage", "An unexpected error occurred");
        return "error/404";
    }
}