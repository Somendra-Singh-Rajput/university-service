package com.apnaclassroom.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {
    private String courseId;
    private String courseName;
    private String deptId;
    private String courseDuration;
}
