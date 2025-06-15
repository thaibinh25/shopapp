package com.project.shopapp.components;

import com.project.shopapp.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.xml.crypto.dsig.Transform;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.SecureRandom;
import java.util.*;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTonkenUtil {

    @Value( "${jwt.expiration}")
    private int expiration;

    @Value( "${jwt.secretKey}")
    private String secretKey;
    public String generateToken(User user) throws Exception{
        //properties => claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());  // 👈 Thêm userId vào claims
        claims.put("role", user.getRole().getName());
        //this.generateSecretKey();// tạo ra secretkey 1 lần rồi tắt để tao khoá chứ ko để chaỵ mãi
        try{
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(String.valueOf(user.getId()))
                    .claim("role",user.getRole().getName())
                    .setExpiration(new Date(System.currentTimeMillis() + expiration *1000L))
                    .signWith(getSignInkey(), SignatureAlgorithm.HS256)
                    .compact();
            return token;
        }catch (Exception e){
           throw new InvalidParameterException("cannot create jwt token, error: "+e.getMessage());
            //return null;
        }
    }

    private Key getSignInkey(){
        byte[] bytes = Decoders.BASE64.decode(secretKey);//Keys.hmacShaKeyFor(Decoders.BASE64.decode("TPeCJ3l2PcMH608zVjEll3F/gRcFEqwDx9YMX57rCfM="));
        return Keys.hmacShaKeyFor(bytes);
    }

    private String generateSecretKey(){
        SecureRandom random = new SecureRandom();
        byte[] keyBytes = new byte[32]; // 256- bit key
        random.nextBytes(keyBytes);
        String secretKey = Encoders.BASE64.encode(keyBytes);
        return secretKey;
    }

    private Claims extracAllClaims(String token){
        return Jwts.parserBuilder()
                .setSigningKey(getSignInkey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public  <T> T extracClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = this.extracAllClaims(token);
        return claimsResolver.apply(claims);
    }

    //chekc expiration
    public boolean isTokenExpired(String token){
        Date expirationDate = this.extracClaim(token, Claims::getExpiration);
        return expirationDate.before(new Date());
    }

    public  String extractPhoneNumber(String token){
        return extracClaim(token,Claims::getSubject);
    }

    public Long extractUserId(String token) {
        String subject = extracClaim(token, Claims::getSubject);
        return Long.parseLong(subject); // subject = userId
    }


    public Boolean validateToken(String token , UserDetails userDetails){
        /*String phoneNumber = extractPhoneNumber(token);
        return (phoneNumber.equals(userDetails.getUsername()) &&
                !isTokenExpired(token));*/

        try {
            Long userIdInToken = getUserIdFromToken(token);
            Long userIdInUser = ((User) userDetails).getId();
            return userIdInToken.equals(userIdInUser) && !isTokenExpired(token);
        } catch (Exception e) {
            System.err.println("Token validation failed: " + e.getMessage());
            return false;
        }
    }


    // Phương thức bổ sung cho WebSocket (không cần truyền UserDetails)
    public Boolean validateToken(String token) {
        try {
            String phoneNumber = extractPhoneNumber(token); // Lấy subject
            return phoneNumber != null && !isTokenExpired(token); // Có phoneNumber và chưa hết hạn
        } catch (Exception e) {
            return false;
        }
    }



    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }


    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        Object userIdObj = claims.get("userId");

        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        } else if (userIdObj instanceof String) {
            return Long.parseLong((String) userIdObj);
        } else {
            throw new IllegalArgumentException("Không tìm thấy userId trong token");
        }
    }
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInkey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


}
