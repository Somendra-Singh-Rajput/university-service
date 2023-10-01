package com.apnaclassroom.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CommonUtility {

    private static final String EMAIL_REGEX =
            "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$";

    public static String getObjectToJson(Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.writeValueAsString(object);
    }

    public static boolean isValidEmail(String email) {
        //Compile the regular expression pattern
        Pattern pattern = Pattern.compile(EMAIL_REGEX);

        //Match the input email against the pattern
        Matcher matcher = pattern.matcher(email);

        //Return true if it's a valid email, false otherwise
        return matcher.matches();
    }
}
