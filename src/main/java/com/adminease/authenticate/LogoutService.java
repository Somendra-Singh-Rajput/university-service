package com.adminease.authenticate;

import com.adminease.dao.TokenDao;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

  private final TokenDao tokenDao;

  private final JwtService jwtService;

  @Override
  public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
    final String authHeader = request.getHeader("Authorization");
    if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
      return;
    }
    var storedToken = tokenDao.findByToken(jwtService.extractTokenId(authHeader.substring(7)))
        .orElse(null);
    if (storedToken != null) {
      storedToken.setExpired(true);
      storedToken.setRevoked(true);
      tokenDao.updateTokenToExpire(storedToken);
      SecurityContextHolder.clearContext();
    }
  }
}
