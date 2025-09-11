package com.srihari.url_shortener.Controller;

import com.srihari.url_shortener.ApplicationProperties;
import com.srihari.url_shortener.Entities.User;
import com.srihari.url_shortener.Exceptions.ShortUrlNotFoundException;
import com.srihari.url_shortener.Models.CreateShortUrlCmd;
import com.srihari.url_shortener.Models.CreateShortUrlForm;
import com.srihari.url_shortener.Entities.ShortURL;
import com.srihari.url_shortener.Models.ShortUrlDto;
import com.srihari.url_shortener.Repositories.ShortUrlRepo;
import com.srihari.url_shortener.Services.ShortUrlService;
import com.srihari.url_shortener.Services.UserService;
import jakarta.validation.Valid;
import org.apache.catalina.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    public ShortUrlService shortUrlService;
    public UserService userService;
    public SecurityUtils securityUtils;

    private final ApplicationProperties properties;

    public HomeController(ApplicationProperties properties) {
        this.properties = properties;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
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
            // Safe way to get current user
            User currentUser = userService.getCurrentUser();

            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Please log in to create short URLs");
                return "redirect:/login";
            }

            // Now safely use currentUser.getId()
            shortUrlService.createShortUrl(form.getOriginalUrl(), currentUser.getId());

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
    String redirectToOriginalUrl(@PathVariable String shortKey, Model model) {
        Optional<ShortURL> shortUrlOptional = shortUrlService.accessShortUrl(shortKey);
        if (shortUrlOptional.isEmpty()) {
            throw new ShortUrlNotFoundException("Invalid short key" + shortKey);
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
        List<ShortURL> shortUrls = shortUrlService.findAllPublicShortUrls();
        model.addAttribute("shortUrls", shortUrls);
        model.addAttribute("baseUrl", properties.baseUrl());
        model.addAttribute("createShortUrlForm", new CreateShortUrlForm(""));
        return "my-urls";
    }


}
