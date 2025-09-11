package com.srihari.url_shortener.Services;

import com.srihari.url_shortener.ApplicationProperties;
import com.srihari.url_shortener.Controller.SecurityUtils;
import com.srihari.url_shortener.Entities.ShortURL;
import com.srihari.url_shortener.Models.CreateShortUrlForm;
import com.srihari.url_shortener.Repositories.ShortUrlRepo;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import com.srihari.url_shortener.Entities.User;
import org.springframework.stereotype.Service;

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

    // Modified Method
    public ShortURL createShortUrl(CreateShortUrlForm createShortUrlForm) {
        ShortURL shortURL = new ShortURL();
        shortURL.setOriginalUrl(createShortUrlForm.originalUrl());
        shortURL.setShortKey(generateUniqueShortKey());
        shortURL.setExpiresAt(Instant.now().plus(properties.defaultExpirationInDays(), ChronoUnit.DAYS));
        shortURL.setCreatedBy(securityUtils.getCurrentUser());  // Associate with the user
        shortURL.setIsPrivate(false);

        return shortUrlRepo.save(shortURL);
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
        Optional<ShortURL> shorUrlOptional = shortUrlRepo.findByShortKey(shortKey);
        if (shorUrlOptional.isEmpty()) {
            return Optional.empty();
        }
        ShortURL shortUrl = shorUrlOptional.get();
        if (shortUrl.getExpiresAt().isBefore(Instant.now())) {
            shortUrlRepo.delete(shortUrl);
            return Optional.empty();
        }
        shortUrl.setClickCount(shortUrl.getClickCount() + 1);
        return Optional.of(shortUrlRepo.save(shortUrl));
    }

    public List<ShortURL> findAllPublicShortUrls() {
        User currentUser = securityUtils.getCurrentUser();
        return shortUrlRepo.findPublicShortUrlsByUser(currentUser.getId());
    }
}
