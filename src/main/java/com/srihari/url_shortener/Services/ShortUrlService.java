package com.srihari.url_shortener.Services;

import com.srihari.url_shortener.ApplicationProperties;
import com.srihari.url_shortener.Controller.SecurityUtils;
import com.srihari.url_shortener.Entities.ShortURL;
import com.srihari.url_shortener.Models.CreateShortUrlForm;
import com.srihari.url_shortener.Repositories.ShortUrlRepo;
import com.srihari.url_shortener.Entities.User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
public class ShortUrlService {

    private final ShortUrlRepo shortUrlRepo;
    private final SecurityUtils securityUtils;
    private final ApplicationProperties properties;

    public ShortUrlService(ShortUrlRepo shortUrlRepo, SecurityUtils securityUtils, ApplicationProperties properties) {
        this.shortUrlRepo = shortUrlRepo;
        this.securityUtils = securityUtils;
        this.properties = properties;
    }

    // Create short URL from form
    public ShortURL createShortUrl(CreateShortUrlForm createShortUrlForm) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User must be authenticated to create short URLs");
        }

        ShortURL shortURL = new ShortURL();
        shortURL.setOriginalUrl(createShortUrlForm.originalUrl());
        shortURL.setShortKey(generateUniqueShortKey());
        shortURL.setExpiresAt(Instant.now().plus(properties.defaultExpirationInDays(), ChronoUnit.DAYS));
        shortURL.setCreatedBy(currentUser);
        shortURL.setIsPrivate(false);
        shortURL.setClickCount(0L); // Initialize click count

        return shortUrlRepo.save(shortURL);
    }

    // Create short URL with just URL string and user ID (for compatibility)
    public ShortURL createShortUrl(String originalUrl, Long userId) {
        CreateShortUrlForm form = new CreateShortUrlForm(originalUrl);
        return createShortUrl(form);
    }

    private String generateUniqueShortKey() {
        String shortKey;
        do {
            shortKey = generateShortKey();
        } while (shortUrlRepo.existsByShortKey(shortKey));
        return shortKey;
    }

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int LENGTH = 6;
    private static final java.security.SecureRandom RANDOM = new java.security.SecureRandom();

    public String generateShortKey() {
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    public Optional<ShortURL> accessShortUrl(String shortKey) {
        Optional<ShortURL> shortUrlOptional = shortUrlRepo.findByShortKey(shortKey);
        if (shortUrlOptional.isEmpty()) {
            return Optional.empty();
        }

        ShortURL shortUrl = shortUrlOptional.get();

        // Check if expired
        if (shortUrl.getExpiresAt() != null && shortUrl.getExpiresAt().isBefore(Instant.now())) {
            shortUrlRepo.delete(shortUrl);
            return Optional.empty();
        }

        // Increment click count
        shortUrl.setClickCount(shortUrl.getClickCount() + 1);
        return Optional.of(shortUrlRepo.save(shortUrl));
    }

    // Get all public short URLs (for home page)
    public List<ShortURL> findAllPublicShortUrls() {
        return shortUrlRepo.findPublicShortUrls();
    }

    // Get user-specific short URLs
    public List<ShortURL> findUserShortUrls(Long userId) {
        return shortUrlRepo.findPublicShortUrlsByUser(userId);
    }
}