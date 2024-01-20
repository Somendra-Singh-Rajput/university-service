package com.adminease.util;

import com.mysql.cj.util.StringUtils;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.Random;

import static com.adminease.enums.Role.*;
import static com.adminease.model.CommonConstants.*;

@Component
public class RandomIdGenerator {
    static String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    static String numbers = "0123456789";
    private static final Random random = new Random();

    public static String generateStudentOrTeacherId(String value, String role) {
        StringBuilder id = getCommonPart();

        if(USER.name().equalsIgnoreCase(role) || TEACHER.name().equalsIgnoreCase(role)){
            if(!StringUtils.isNullOrEmpty(value) && value.length() >= 2){
                String first2Char = value.substring(0, 2);
                id.append(first2Char);
            } else {
                //Add one random letters
                for (int i = 0; i < 2; i++) {
                    int randomIndex = random.nextInt(letters.length());
                    id.append(letters.charAt(randomIndex));
                }
            }
        }

        //Add five random numbers
        for (int i = 0; i < 4; i++) {
            int randomIndex = random.nextInt(numbers.length());
            id.append(numbers.charAt(randomIndex));
        }

        //Add two random letters
        for (int i = 0; i < 2; i++) {
            int randomIndex = random.nextInt(letters.length());
            id.append(letters.charAt(randomIndex));
        }
        return id.toString();
    }

    public static String generateAdminOrManagerId(String role) {
        StringBuilder id = getCommonPart();

        if(ADMIN.name().equalsIgnoreCase(role)){
            id.append("AD");
        } else if (MANAGER.name().equalsIgnoreCase(role)){
            id.append("MT");
        }

        //Add five random numbers
        for (int i = 0; i < 4; i++) {
            int randomIndex = random.nextInt(numbers.length());
            id.append(numbers.charAt(randomIndex));
        }

        //Add one random letters
        for (int i = 0; i < 2; i++) {
            int randomIndex = random.nextInt(letters.length());
            id.append(letters.charAt(randomIndex));
        }
        return id.toString();
    }

    public static String generateId(String type) {
        StringBuilder id = getCommonPart();
        if(DEPARTMENT.equalsIgnoreCase(type)){
            id.append("DP");
        } else if (SEMESTER.equalsIgnoreCase(type)) {
            id.append("SEM");
        } else if (COURSE.equalsIgnoreCase(type)) {
            id.append("CR");
        } else if(SUBJECT.equalsIgnoreCase(type)) {
            id.append("SBJ");
        }

        //Add five random numbers
        for (int i = 0; i < 3; i++) {
            int randomIndex = random.nextInt(numbers.length());
            id.append(numbers.charAt(randomIndex));
        }

        //Add two random letters
        for (int i = 0; i < 2; i++) {
            int randomIndex = random.nextInt(letters.length());
            id.append(letters.charAt(randomIndex));
        }
        return id.toString();
    }

    private static StringBuilder getCommonPart(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Year.now().getValue()%100);
        return stringBuilder;
    }
}
