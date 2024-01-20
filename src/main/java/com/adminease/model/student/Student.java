package com.adminease.model.student;

import com.adminease.enums.Role;
import com.adminease.model.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    private String studentId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String dob;
    private String gender;
    private String fatherName;
    private String motherName;
    private Address address;
    private String courseId;
    private String departmentId;
    private String profilePhoto;
    private String createTs;
    private String updateTs;
    private String createBy;
    private String updateBy;
    private String password;
    private String oldPassword;
    private Role role;
    private boolean enabled;
    private String doa;
    private String dop;
}
