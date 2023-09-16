package com.apnaclassroom.model.user;

import com.apnaclassroom.enums.TokenType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
