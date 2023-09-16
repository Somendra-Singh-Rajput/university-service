package com.apnaclassroom.config;

import com.apnaclassroom.authenticate.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import static com.apnaclassroom.enums.Permission.*;
import static com.apnaclassroom.enums.Role.*;
import static com.apnaclassroom.model.CommonConstants.*;
import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;
  private final AuthenticationProvider authenticationProvider;
  private final LogoutHandler logoutHandler;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf()
        .disable()
        .authorizeHttpRequests()
            .requestMatchers(
                "/api/v1/auth/**",
                "/v2/api-docs",
                "/v3/api-docs",
                "/v3/api-docs/**",
                "/swagger-resources",
                "/swagger-resources/**",
                "/configuration/ui",
                "/configuration/security",
                "/swagger-ui/**",
                "/webjars/**",
                "/swagger-ui.html/**"
        ).permitAll()

         //These paths are accessible by anyone who has below roles
        .requestMatchers(MANAGER_ACCESS_PATH, TEACHER_ACCESS_PATH, STUDENT_ACCESS_PATH, DEPARTMENT_ACCESS_PATH, SEMESTER_ACCESS_PATH, COURSE_ACCESS_PATH, SUBJECT_ACCESS_PATH)
            .hasAnyRole(ADMIN.name(), MANAGER.name(), TEACHER.name(), USER.name())

         //But given roles can access read and update endPoints
        .requestMatchers(GET, MANAGER_ACCESS_PATH, TEACHER_ACCESS_PATH, STUDENT_ACCESS_PATH, DEPARTMENT_ACCESS_PATH, SEMESTER_ACCESS_PATH, COURSE_ACCESS_PATH, SUBJECT_ACCESS_PATH)
            .hasAnyAuthority(ADMIN_READ.name(), MANAGER_READ.name())
        .requestMatchers(PUT, MANAGER_ACCESS_PATH, TEACHER_ACCESS_PATH, STUDENT_ACCESS_PATH, DEPARTMENT_ACCESS_PATH, SEMESTER_ACCESS_PATH, COURSE_ACCESS_PATH, SUBJECT_ACCESS_PATH)
            .hasAnyAuthority(ADMIN_UPDATE.name(), MANAGER_UPDATE.name())

         //But below two roles can access delete and create endPoints
        .requestMatchers(POST, MANAGER_ACCESS_PATH, TEACHER_ACCESS_PATH, STUDENT_ACCESS_PATH, DEPARTMENT_ACCESS_PATH, SEMESTER_ACCESS_PATH, COURSE_ACCESS_PATH, SUBJECT_ACCESS_PATH)
            .hasAnyAuthority(ADMIN_CREATE.name(), MANAGER_CREATE.name())
        .requestMatchers(DELETE, MANAGER_ACCESS_PATH, TEACHER_ACCESS_PATH, STUDENT_ACCESS_PATH, DEPARTMENT_ACCESS_PATH, SEMESTER_ACCESS_PATH, COURSE_ACCESS_PATH, SUBJECT_ACCESS_PATH)
            .hasAnyAuthority(ADMIN_DELETE.name(), MANAGER_DELETE.name())

            .requestMatchers(GET, TEACHER_ACCESS_PATH).hasAnyAuthority(TEACHER_READ.name())
            .requestMatchers(PUT, TEACHER_ACCESS_PATH).hasAnyAuthority(TEACHER_UPDATE.name())

            .requestMatchers(GET, STUDENT_ACCESS_PATH).hasAnyAuthority(USER_READ.name())
            .requestMatchers(PUT, STUDENT_ACCESS_PATH).hasAnyAuthority(USER_UPDATE.name())

            //Only admin can access below endPoints
        .requestMatchers(ADMIN_ACCESS_PATH).hasRole(ADMIN.name())
        .requestMatchers(GET, ADMIN_ACCESS_PATH).hasAuthority(ADMIN_READ.name())
        .requestMatchers(POST, ADMIN_ACCESS_PATH).hasAuthority(ADMIN_CREATE.name())
        .requestMatchers(PUT, ADMIN_ACCESS_PATH).hasAuthority(ADMIN_UPDATE.name())
        .requestMatchers(DELETE, ADMIN_ACCESS_PATH).hasAuthority(ADMIN_DELETE.name())

            .anyRequest()
          .authenticated()
        .and()
          .sessionManagement()
          .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .authenticationProvider(authenticationProvider)
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .logout()
        .logoutUrl("/api/v1/auth/logout")
        .addLogoutHandler(logoutHandler)
        .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext());

    return http.build();
  }

  @Bean
  public AccessDeniedHandler accessDeniedHandler() {
    return (request, response, accessDeniedException) -> {
        throw accessDeniedException;
    };
  }
}
