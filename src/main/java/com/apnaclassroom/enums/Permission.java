package com.apnaclassroom.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Permission {

    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_CREATE("admin:create"),
    ADMIN_DELETE("admin:delete"),

    MANAGER_READ("management:read"),
    MANAGER_UPDATE("management:update"),
    MANAGER_CREATE("management:create"),
    MANAGER_DELETE("management:delete"),

    TEACHER_READ("teacher:read"),
    TEACHER_UPDATE("teacher:update"),
    TEACHER_CREATE("teacher:create"),
    TEACHER_DELETE("teacher:delete"),

    LIBRARIAN_READ("librarian:read"),
    LIBRARIAN_UPDATE("librarian:update"),
    LIBRARIAN_CREATE("librarian:create"),
    LIBRARIAN_DELETE("librarian:delete"),

    HOSTEL_MANAGEMENT_READ("hostelManagement:read"),
    HOSTEL_MANAGEMENT_UPDATE("hostelManagement:update"),
    HOSTEL_MANAGEMENT_CREATE("hostelManagement:create"),
    HOSTEL_MANAGEMENT_DELETE("hostelManagement:delete"),

    USER_READ("user:read"),
    USER_UPDATE("user:update"),
    USER_CREATE("user:create"),
    USER_DELETE("user:delete");

    private final String permission;
}
