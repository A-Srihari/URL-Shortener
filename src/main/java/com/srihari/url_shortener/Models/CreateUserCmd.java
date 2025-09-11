package com.srihari.url_shortener.Models;

public record CreateUserCmd(
        String email,
        String password,
        String name
) {

}