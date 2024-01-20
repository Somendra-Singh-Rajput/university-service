package com.adminease.model.user;

import com.adminease.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    private String username;
    private String email;
    private String password;
    private String oldPassword;
    private boolean enabled;
    private String createTs;
    private String updateTs;
    @Enumerated(EnumType.STRING)
    private Role role;
}
