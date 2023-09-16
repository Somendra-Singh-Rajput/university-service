package com.apnaclassroom.model.management;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {
    private String deptId;
    private String deptName;
    private String createTs;
    private String updateTs;
    private String createBy;
    private String updateBy;
}
