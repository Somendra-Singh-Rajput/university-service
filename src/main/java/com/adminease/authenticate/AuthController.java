package com.adminease.authenticate;

import com.adminease.dao.TokenDao;
import com.adminease.model.Status;
import com.adminease.model.user.RegisterRequest;
import com.adminease.model.user.RegisterResponse;
import com.adminease.service.EmailService;
import com.mysql.cj.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

  private final AuthService authService;

  private final LogoutService logoutService;

  private final EmailService emailService;

  private final TokenDao tokenDao;

  @PostMapping("/register")
  @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
  public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
    return ResponseEntity.ok(authService.register(request));
  }
  @PostMapping("/authenticate")
  public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest request) {
    return ResponseEntity.ok(authService.authenticate(request));
  }

  @PostMapping("/refresh-token")
  public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
    authService.refreshToken(request, response);
  }

  @GetMapping("/logout")
  public RegisterResponse logout(HttpServletRequest request, HttpServletResponse response) {
    logoutService.logout(request, response, SecurityContextHolder.getContext().getAuthentication());

    return RegisterResponse.builder()
            .status(HttpStatus.OK.name())
            .message("Logged out successfully!").build();
  }

  @PostMapping("/sendTestEmail")
  public String sendTestEmail(@RequestBody String email) {

    if(StringUtils.isNullOrEmpty(email)){
      return "Email address can't be empty";
    }

    String subject = "Test Email";
    String text = "This is a test email sent from a Spring Boot application.";

    boolean emailStatus = emailService.sendEmail(email, subject, text);
    if(emailStatus){
      return "Email sent successfully!";
    } else{
      return "Failed to send an email!";
    }
  }

  //@Scheduled(fixedRate = 600000) // Run every 10 minutes (10 * 60,000 milliseconds)
  @GetMapping("/deleteExpiredTokens")
  public void deleteExpiredTokens() {
    LOG.info("Expired token cleanup scheduled method triggered");
    int deleteCount = tokenDao.deleteExpiredTokens();
    if(deleteCount > 0){
      LOG.info("{} expired tokens deleted from table", deleteCount);
    }
  }

  @GetMapping("/health")
  public ResponseEntity<Status> getHealthStatus() {
    return ResponseEntity.ok(Status.builder().status("UP").statusCode(HttpStatus.OK.toString()).build());
  }
}
