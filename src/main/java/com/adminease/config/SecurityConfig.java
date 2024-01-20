package com.adminease.config;

import com.adminease.authenticate.JwtAuthFilter;
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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static com.adminease.enums.Permission.*;
import static com.adminease.enums.Role.*;
import static com.adminease.model.CommonConstants.*;
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
                new AntPathRequestMatcher("/api/v1/auth/**"),
                new AntPathRequestMatcher("/v2/api-docs"),
                new AntPathRequestMatcher("/v3/api-docs"),
                new AntPathRequestMatcher("/v3/api-docs/**"),
                new AntPathRequestMatcher("/swagger-resources"),
                new AntPathRequestMatcher("/swagger-resources/**"),
                new AntPathRequestMatcher("/configuration/ui"),
                new AntPathRequestMatcher("/configuration/security"),
                new AntPathRequestMatcher("/swagger-ui/**"),
                new AntPathRequestMatcher("/webjars/**"),
                new AntPathRequestMatcher("/swagger-ui.html/**"),
                new AntPathRequestMatcher("/actuator/**")
        ).permitAll()

         //These paths are accessible by anyone who has below roles
        .requestMatchers(new AntPathRequestMatcher(MANAGER_ACCESS_PATH),
                new AntPathRequestMatcher(TEACHER_ACCESS_PATH),
                new AntPathRequestMatcher(STUDENT_ACCESS_PATH),
                new AntPathRequestMatcher(DEPARTMENT_ACCESS_PATH),
                new AntPathRequestMatcher(SEMESTER_ACCESS_PATH),
                new AntPathRequestMatcher(COURSE_ACCESS_PATH),
                new AntPathRequestMatcher(SUBJECT_ACCESS_PATH))
            .hasAnyRole(ADMIN.name(), MANAGER.name(), TEACHER.name(), USER.name())

         //But given roles can access read and update endPoints
        .requestMatchers(new AntPathRequestMatcher(MANAGER_ACCESS_PATH, GET.name()),
                new AntPathRequestMatcher(TEACHER_ACCESS_PATH, GET.name()),
                new AntPathRequestMatcher(STUDENT_ACCESS_PATH, GET.name()),
                new AntPathRequestMatcher(DEPARTMENT_ACCESS_PATH, GET.name()),
                new AntPathRequestMatcher(SEMESTER_ACCESS_PATH, GET.name()),
                new AntPathRequestMatcher(COURSE_ACCESS_PATH, GET.name()),
                new AntPathRequestMatcher(SUBJECT_ACCESS_PATH, GET.name()))
            .hasAnyAuthority(ADMIN_READ.name(), MANAGER_READ.name())
        .requestMatchers(
                new AntPathRequestMatcher(MANAGER_ACCESS_PATH, PUT.name()),
                new AntPathRequestMatcher(TEACHER_ACCESS_PATH, PUT.name()),
                new AntPathRequestMatcher(STUDENT_ACCESS_PATH, PUT.name()),
                new AntPathRequestMatcher(DEPARTMENT_ACCESS_PATH, PUT.name()),
                new AntPathRequestMatcher(SEMESTER_ACCESS_PATH, PUT.name()),
                new AntPathRequestMatcher(COURSE_ACCESS_PATH, PUT.name()),
                new AntPathRequestMatcher(SUBJECT_ACCESS_PATH, PUT.name()))
            .hasAnyAuthority(ADMIN_UPDATE.name(), MANAGER_UPDATE.name())

         //But below two roles can access delete and create endPoints
        .requestMatchers(
                new AntPathRequestMatcher(MANAGER_ACCESS_PATH, POST.name()),
                new AntPathRequestMatcher(TEACHER_ACCESS_PATH, POST.name()),
                new AntPathRequestMatcher(STUDENT_ACCESS_PATH, POST.name()),
                new AntPathRequestMatcher(DEPARTMENT_ACCESS_PATH, POST.name()),
                new AntPathRequestMatcher(SEMESTER_ACCESS_PATH, POST.name()),
                new AntPathRequestMatcher(COURSE_ACCESS_PATH, POST.name()),
                new AntPathRequestMatcher(SUBJECT_ACCESS_PATH, POST.name()))
            .hasAnyAuthority(ADMIN_CREATE.name(), MANAGER_CREATE.name())
        .requestMatchers(
                new AntPathRequestMatcher(MANAGER_ACCESS_PATH, DELETE.name()),
                new AntPathRequestMatcher(TEACHER_ACCESS_PATH, DELETE.name()),
                new AntPathRequestMatcher(STUDENT_ACCESS_PATH, DELETE.name()),
                new AntPathRequestMatcher(DEPARTMENT_ACCESS_PATH, DELETE.name()),
                new AntPathRequestMatcher(SEMESTER_ACCESS_PATH, DELETE.name()),
                new AntPathRequestMatcher(COURSE_ACCESS_PATH, DELETE.name()),
                new AntPathRequestMatcher(SUBJECT_ACCESS_PATH, DELETE.name()))
            .hasAnyAuthority(ADMIN_DELETE.name(), MANAGER_DELETE.name())

            .requestMatchers(new AntPathRequestMatcher(TEACHER_ACCESS_PATH, GET.name())).hasAnyAuthority(TEACHER_READ.name())
            .requestMatchers(new AntPathRequestMatcher(TEACHER_ACCESS_PATH, PUT.name())).hasAnyAuthority(TEACHER_UPDATE.name())

            .requestMatchers(new AntPathRequestMatcher(STUDENT_ACCESS_PATH,GET.name())).hasAnyAuthority(USER_READ.name())
            .requestMatchers(new AntPathRequestMatcher(STUDENT_ACCESS_PATH, PUT.name())).hasAnyAuthority(USER_UPDATE.name())

            //Only admin can access below endPoints
        .requestMatchers(new AntPathRequestMatcher(ADMIN_ACCESS_PATH)).hasRole(ADMIN.name())
        .requestMatchers(new AntPathRequestMatcher(ADMIN_ACCESS_PATH, GET.name())).hasAuthority(ADMIN_READ.name())
        .requestMatchers(new AntPathRequestMatcher(ADMIN_ACCESS_PATH, POST.name())).hasAuthority(ADMIN_CREATE.name())
        .requestMatchers(new AntPathRequestMatcher(ADMIN_ACCESS_PATH, PUT.name())).hasAuthority(ADMIN_UPDATE.name())
        .requestMatchers(new AntPathRequestMatcher(ADMIN_ACCESS_PATH, DELETE.name())).hasAuthority(ADMIN_DELETE.name())

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
