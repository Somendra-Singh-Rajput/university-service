package com.adminease.model.management;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Semester {
    private String semId;
    private String semName;
    private String createTs;
    private String updateTs;
    private String createBy;
    private String updateBy;
}
