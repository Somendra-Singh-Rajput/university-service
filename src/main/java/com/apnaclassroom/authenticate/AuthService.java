package com.apnaclassroom.authenticate;

import com.apnaclassroom.dao.TokenDao;
import com.apnaclassroom.dao.UserDao;
import com.apnaclassroom.enums.TokenType;
import com.apnaclassroom.model.user.RegisterRequest;
import com.apnaclassroom.model.user.RegisterResponse;
import com.apnaclassroom.model.user.Token;
import com.apnaclassroom.model.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

  private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);

  private final UserDao userDao;

  private final TokenDao tokenDao;

  private final PasswordEncoder passwordEncoder;

  private final JwtService jwtService;

  private final AuthenticationManager authenticationManager;

  public RegisterResponse register(RegisterRequest request) {
    LOG.info("Registering a new user for userId: {}", request.getUsername());

    try{
      var user = User.builder()
              .username(request.getUsername())
              .email(request.getEmail())
              .role(request.getRole())
              .enabled(true)
              .password(passwordEncoder.encode(request.getPassword()))
              .build();

      UserDetails  userDetails = userDao.saveUser(user);

      return RegisterResponse.builder().status(HttpStatus.OK.name())
              .message("New user registered successfully!, For userId: "+userDetails.getUsername()).build();
    } catch (Exception ex){
      LOG.error("An error occurred while registering new user for userId: {}, Exception: {}",
              request.getUsername(), ExceptionUtils.getStackTrace(ex));
      return RegisterResponse.builder().status(HttpStatus.INTERNAL_SERVER_ERROR.name())
              .message("New user registration failed!, For userId: "+request.getUsername()).build();
    }
  }

  public AuthResponse authenticate(AuthRequest request) {
    authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

    var user = userDao.getUserDataById(request.getUsername());

    if(user.isPresent()){
      //Generating tokenId to save the token into db with this tokenId
      String tokenId = UUID.randomUUID().toString();
      user.get().setTokenId(tokenId);
      var jwtToken = jwtService.generateToken(user.get(), tokenId);
      var refreshToken = jwtService.generateRefreshToken(user.get(), tokenId);
      revokeAllUserTokens(user.get());
      saveUserToken(user.get(), jwtToken);
      return AuthResponse.builder().userId(request.getUsername()+": Authenticated successfully!")
              .role(String.valueOf(user.get().getRole()))
              .accessToken(jwtToken).refreshToken(refreshToken).build();
    } else {
      return AuthResponse.builder().userId(request.getUsername()+": Authentication failed!")
              .role("No role retrieved!")
              .accessToken("No Access Token Generated!").refreshToken("No Refresh Token Generated!").build();
    }
  }

  private void saveUserToken(User user, String jwtToken) {
    var token = Token.builder()
        .user(user)
        .tokenId(user.getTokenId())
        .tokenValue(jwtToken)
        .tokenType(TokenType.BEARER)
        .expired(false)
        .revoked(false)
        .build();
    tokenDao.saveToken(token);
  }

  public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
    final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    final String refreshToken;
    final String username;

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return;
    }

    refreshToken = authHeader.substring(7);
    username = jwtService.extractUsername(refreshToken);

    if (username != null) {
      var user = this.userDao.getUserDataById(username);
      if(user.isPresent() && jwtService.isTokenValid(refreshToken, user.get())){
          var accessToken = jwtService.generateToken(user.get(), user.get().getTokenId());

          revokeAllUserTokens(user.get());
          saveUserToken(user.get(), accessToken);
          var authResponse = AuthResponse.builder()
                  .accessToken(accessToken)
                  .refreshToken(refreshToken)
                  .build();
          new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
      }
    }
  }

  private void revokeAllUserTokens(User user) {
    var validUserTokens = tokenDao.findAllValidTokenByUser(user.getUsername());
    if (validUserTokens.isEmpty())
      return;
    validUserTokens.forEach(token -> {
      token.setExpired(true);
      token.setRevoked(true);
    });
    tokenDao.updateAllValidTokensToExpire(validUserTokens);
  }
}
