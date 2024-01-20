package com.adminease.model;

import com.adminease.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admin {

    private String adminId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String dob;
    private String gender;
    private String fatherName;
    private String motherName;
    private Address address;
    private String doj;
    private String dol;
    private String profilePhoto;
    private String password;
    private String oldPassword;
    private Role role;
    private boolean enabled;
    private String position;
    private String reportingTo;
    private String officeLocation;
    private String officialEmail;
    private String createTs;
    private String updateTs;
    private String createBy;
    private String updateBy;
}
