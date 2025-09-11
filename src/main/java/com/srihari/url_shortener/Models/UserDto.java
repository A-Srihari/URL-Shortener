package com.srihari.url_shortener.Models;

import java.io.Serializable;

public record UserDto(Long id, String name) implements Serializable {
}