package com.apnaclassroom.model.teacher;

import com.apnaclassroom.enums.Role;
import com.apnaclassroom.model.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Teacher {

    private String teacherId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String dob;
    private String gender;
    private String fatherName;
    private String motherName;
    private Address address;
    private String expertise;
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
    private String doj;
    private String dol;
    private String position;
    private String reportingTo;
    private String officeLocation;
    private String officialEmail;
}
