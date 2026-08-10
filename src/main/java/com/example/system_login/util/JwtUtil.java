package com.example.system_login.util;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String SECRET_KEY;

    private final String ISSUER = "com.example.system_login";

    private final long EXPIRATION_TIME = 3600000;

    public Algorithm getAlgorithm() {
        return Algorithm.HMAC256(SECRET_KEY);
    }

    public String generateToken(String username) {
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(getAlgorithm());
    }

    public String getUsernameFromToken(String token) {
        return decodeToken(token).getSubject();
    }


    public boolean validateToken(String token, String username) {
        try {
            DecodedJWT jwt = decodeToken(token);
            return (jwt.getSubject().equals(username) && !isTokenExpired(jwt));
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    private boolean isTokenExpired(DecodedJWT decodedJWT) {
        Date expirationDate = decodedJWT.getExpiresAt();
        return expirationDate.before(new Date());
    }

    private DecodedJWT decodeToken(String token) {
        return JWT.require(getAlgorithm())
                .withIssuer(ISSUER)
                .build()
                .verify(token);
    }

}
