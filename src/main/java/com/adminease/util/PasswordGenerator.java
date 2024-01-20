package com.adminease.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PasswordGenerator {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;

    public static String generateRandomPassword() {
        StringBuilder password = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = random.nextInt(ALPHANUMERIC.length());
            password.append(ALPHANUMERIC.charAt(index));
        }

        return password.toString();
    }

    public static String maskPassword(String password, char maskingChar) {
        int passwordLength = password.length();
        StringBuilder maskedPassword = new StringBuilder(passwordLength);

        //Replace each character of the password with the masking character
        for (int i = 0; i < passwordLength; i++) {
            maskedPassword.append(maskingChar);
        }

        return maskedPassword.toString();
    }
}
