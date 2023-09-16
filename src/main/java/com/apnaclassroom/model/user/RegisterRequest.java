package com.apnaclassroom.model.user;

import com.apnaclassroom.enums.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
