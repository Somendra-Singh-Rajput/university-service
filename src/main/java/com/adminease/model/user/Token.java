package com.adminease.model.user;

import com.adminease.enums.TokenType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Token {
  private String tokenId;
  private User user;
  private String tokenValue;
  @Enumerated(EnumType.STRING)
  private TokenType tokenType = TokenType.BEARER;
  private boolean revoked;
  private boolean expired;
  private String createTs;
  private String updateTs;
}
