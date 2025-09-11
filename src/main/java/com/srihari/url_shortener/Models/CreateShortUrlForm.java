package com.srihari.url_shortener.Models;

import jakarta.validation.constraints.NotBlank;

public record CreateShortUrlForm(
        @NotBlank(message = "Original URL is required")
        String originalUrl) {
    public Object getOriginalUrl() {
        return originalUrl;
    }
}
