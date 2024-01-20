package com.adminease.model.management;

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
    private String createTs;
    private String updateTs;
    private String createBy;
    private String updateBy;
}
