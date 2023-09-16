package com.apnaclassroom.authenticate;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final Logger LOG = LoggerFactory.getLogger(JwtService.class);

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        LOG.info("Extracting claims from token...");
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails, String tokenId) {
        return generateToken(new HashMap<>(), userDetails, tokenId);
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails, String tokenId) {
        LOG.info("Generating access token for userId: {}", userDetails.getUsername());
        return buildToken(extraClaims, userDetails, jwtExpiration, tokenId);
    }

    public String generateRefreshToken(UserDetails userDetails, String tokenId) {
        LOG.info("Generating refresh access token for userId: {}", userDetails.getUsername());
        return buildToken(new HashMap<>(), userDetails, refreshExpiration, tokenId);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration, String tokenId) {
        LOG.info("Generating access token...");
        extraClaims.put("jti", tokenId);
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        LOG.info("Checking if access token is valid...");
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        LOG.info("Checking if access token is expired...");
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        LOG.info("Extracting access token expiration time...");
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        LOG.info("Extracting all claims from the token...");
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes) ;
    }

    public String extractTokenId(String jwtToken) {
        LOG.info("Extracting tokenId...");
        try {
            //Parse the JWT token
            Jws<Claims> jws = Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(jwtToken);

            //Extract the JWT ID (JTI) from the claims
            return jws.getBody().getId();
        } catch (Exception e) {
            // Handle parsing or verification errors
            LOG.error("Error occurred while extracting tokenId, Exception: {}", ExceptionUtils.getStackTrace(e));
            return null;
        }
    }
}
