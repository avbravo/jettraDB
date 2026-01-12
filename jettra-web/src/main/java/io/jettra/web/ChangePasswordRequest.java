package io.jettra.web;

public record ChangePasswordRequest(String username, String oldPassword, String newPassword) {}
