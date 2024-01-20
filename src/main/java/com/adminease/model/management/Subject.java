package com.adminease.model.management;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subject {
    private String subjId;
    private String subjName;
    private String courseId;
    private String semId;
    private String createTs;
    private String updateTs;
    private String createBy;
    private String updateBy;
}
