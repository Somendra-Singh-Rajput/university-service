package com.adminease.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Subject {
    private String subjId;
    private String subjName;
    private String courseId;
    private String deptId;
}
