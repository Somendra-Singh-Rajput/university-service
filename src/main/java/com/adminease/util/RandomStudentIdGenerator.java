package com.adminease.util;

import com.mysql.cj.util.StringUtils;
import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.Random;

@Component
public class RandomStudentIdGenerator {

    public static String generateStudentID(String courseId) {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numbers = "0123456789";
        Random random = new Random();

        StringBuilder studentID = new StringBuilder();
        studentID.append(Year.now().getValue()%100);

        if(!StringUtils.isNullOrEmpty(courseId) && courseId.length() >= 2){
            String first2Char = courseId.substring(0, 2);
            studentID.append(first2Char);
        } else {
            //Add one random letters
            for (int i = 0; i < 2; i++) {
                int randomIndex = random.nextInt(letters.length());
                studentID.append(letters.charAt(randomIndex));
            }
        }

        //Add five random numbers
        for (int i = 0; i < 4; i++) {
            int randomIndex = random.nextInt(numbers.length());
            studentID.append(numbers.charAt(randomIndex));
        }

        //Add one random letters
        for (int i = 0; i < 2; i++) {
            int randomIndex = random.nextInt(letters.length());
            studentID.append(letters.charAt(randomIndex));
        }

        return studentID.toString();
    }
}
