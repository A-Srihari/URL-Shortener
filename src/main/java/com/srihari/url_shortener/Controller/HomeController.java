package com.srihari.url_shortener.Controller;

import com.srihari.url_shortener.ApplicationProperties;
import com.srihari.url_shortener.Entities.User;
import com.srihari.url_shortener.Exceptions.ShortUrlNotFoundException;
import com.srihari.url_shortener.Models.CreateShortUrlForm;
import com.srihari.url_shortener.Entities.ShortURL;
import com.srihari.url_shortener.Services.ShortUrlService;
import com.srihari.url_shortener.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    private final ShortUrlService shortUrlService;
    private final UserService userService;
    private final SecurityUtils securityUtils;
    private final ApplicationProperties properties;

    // Constructor injection - this is the proper way to inject dependencies
    public HomeController(ShortUrlService shortUrlService,
                          UserService userService,
                          SecurityUtils securityUtils,
                          ApplicationProperties properties) {
        this.shortUrlService = shortUrlService;
        this.userService = userService;
        this.securityUtils = securityUtils;
        this.properties = properties;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        // Show all public URLs for home page (not user-specific)
        List<ShortURL> shortUrls = shortUrlService.findAllPublicShortUrls();
        model.addAttribute("shortUrls", shortUrls);
        model.addAttribute("baseUrl", properties.baseUrl());
        model.addAttribute("createShortUrlForm", new CreateShortUrlForm(""));
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @PostMapping("/short-urls")
    public String createShortUrl(@ModelAttribute CreateShortUrlForm form,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Get current user safely
            User currentUser = userService.getCurrentUser();

            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Please log in to create short URLs");
                return "redirect:/login";
            }

            // Create the short URL using the form
            shortUrlService.createShortUrl(form);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Short URL created successfully!");
            return "redirect:/my-urls";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Error creating short URL: " + e.getMessage());
            return "redirect:/my-urls";
        }
    }

    @GetMapping("/s/{shortKey}")
    public String redirectToOriginalUrl(@PathVariable String shortKey, Model model) {
        Optional<ShortURL> shortUrlOptional = shortUrlService.accessShortUrl(shortKey);
        if (shortUrlOptional.isEmpty()) {
            throw new ShortUrlNotFoundException("Invalid short key: " + shortKey);
        }
        ShortURL shortURL = shortUrlOptional.get();
        return "redirect:" + shortURL.getOriginalUrl();
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/my-urls")
    public String showUserUrls(Model model) {
        try {
            User currentUser = userService.getCurrentUser();
            if (currentUser == null) {
                return "redirect:/login?error=authentication_required";
            }

            // Get user-specific URLs
            List<ShortURL> shortUrls = shortUrlService.findUserShortUrls(currentUser.getId());
            model.addAttribute("shortUrls", shortUrls);
            model.addAttribute("baseUrl", properties.baseUrl());
            model.addAttribute("createShortUrlForm", new CreateShortUrlForm(""));
            return "my-urls";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading your URLs: " + e.getMessage());
            return "redirect:/home";
        }
    }
}