package com.apnaclassroom.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.apnaclassroom.enums.Permission.*;

@Getter
@RequiredArgsConstructor
public enum Role {

  USER(Set.of(
          USER_READ,
          USER_UPDATE,
          USER_CREATE,
          USER_DELETE
  )),

  TEACHER(Set.of(
          TEACHER_READ,
          TEACHER_UPDATE,
          TEACHER_CREATE,
          TEACHER_DELETE,

          USER_READ,
          USER_UPDATE,
          USER_CREATE,
          USER_DELETE
  )),

  LIBRARIAN(Set.of(
          LIBRARIAN_READ,
          LIBRARIAN_UPDATE,
          LIBRARIAN_CREATE,
          LIBRARIAN_DELETE,

          USER_READ,
          USER_UPDATE,
          USER_CREATE,
          USER_DELETE
  )),

  HOSTEL_MANAGEMENT(Set.of(
          HOSTEL_MANAGEMENT_READ,
          HOSTEL_MANAGEMENT_UPDATE,
          HOSTEL_MANAGEMENT_CREATE,
          HOSTEL_MANAGEMENT_DELETE,

          USER_READ,
          USER_UPDATE,
          USER_CREATE,
          USER_DELETE
  )),

  MANAGER(
          Set.of(
                  MANAGER_READ,
                  MANAGER_UPDATE,
                  MANAGER_DELETE,
                  MANAGER_CREATE,

                  TEACHER_READ,
                  TEACHER_UPDATE,
                  TEACHER_CREATE,
                  TEACHER_DELETE,

                  LIBRARIAN_READ,
                  LIBRARIAN_UPDATE,
                  LIBRARIAN_CREATE,
                  LIBRARIAN_DELETE,

                  HOSTEL_MANAGEMENT_READ,
                  HOSTEL_MANAGEMENT_UPDATE,
                  HOSTEL_MANAGEMENT_CREATE,
                  HOSTEL_MANAGEMENT_DELETE,

                  USER_READ,
                  USER_UPDATE,
                  USER_DELETE,
                  USER_CREATE
          )
  ),

  ADMIN(
          Set.of(
                  ADMIN_READ,
                  ADMIN_UPDATE,
                  ADMIN_DELETE,
                  ADMIN_CREATE,

                  MANAGER_READ,
                  MANAGER_UPDATE,
                  MANAGER_DELETE,
                  MANAGER_CREATE,

                  TEACHER_READ,
                  TEACHER_UPDATE,
                  TEACHER_CREATE,
                  TEACHER_DELETE,

                  LIBRARIAN_READ,
                  LIBRARIAN_UPDATE,
                  LIBRARIAN_CREATE,
                  LIBRARIAN_DELETE,

                  HOSTEL_MANAGEMENT_READ,
                  HOSTEL_MANAGEMENT_UPDATE,
                  HOSTEL_MANAGEMENT_CREATE,
                  HOSTEL_MANAGEMENT_DELETE,

                  USER_READ,
                  USER_UPDATE,
                  USER_DELETE,
                  USER_CREATE
          )
  );

  private final Set<Permission> permissions;

  public List<SimpleGrantedAuthority> getAuthorities() {
    List<SimpleGrantedAuthority> authorities = getPermissions()
            .stream()
            .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
            .collect(Collectors.toList());
    authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
    return authorities;
  }
}
