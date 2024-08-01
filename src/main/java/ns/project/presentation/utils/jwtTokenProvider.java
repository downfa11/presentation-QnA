package ns.project.presentation.utils;


import io.jsonwebtoken.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;


@Component
public class jwtTokenProvider {

    private String jwtSecret; // secret key
    private long jwtExpirationInMs;

    public jwtTokenProvider() {
        this.jwtSecret = "NYd4nEtyLtcU7cpS/1HTFVmQJd7MmrP+HafWoXZjWNOL7qKccOOUfQNEx5yvG6dfdpuBeyMs9eEbRmdBrPQCNg==";
        this.jwtExpirationInMs = 1000L * 60 * 120;
    }


    public void setJwtCookie(HttpServletResponse response, String jwtToken) {
        Cookie jwtCookie = new Cookie("jwtToken", jwtToken);
        jwtCookie.setHttpOnly(true); // JavaScript에서 접근 불가
        jwtCookie.setSecure(false); // HTTPS에서만 전송, local환경
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge((int) jwtExpirationInMs);
        response.addCookie(jwtCookie);
    }

    public String getJwtFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwtToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public String getMembershipIdbyToken() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String token = getJwtFromCookie(request);
        if (token == null || token.length() == 0) {
            System.out.println("JwtToken is Invalid.");
            return "";
        }

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public String generateJwtToken(String membershipId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        String token = Jwts.builder()
                .setSubject(membershipId.toString())
                .setHeaderParam("type", "jwt")
                .claim("id", membershipId)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();

        return token;
    }



    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException ex) {
            // Invalid JWT token: 유효하지 않은 JWT 토큰일 때 발생하는 예외
        } catch (ExpiredJwtException ex) {
            // Expired JWT token: 토큰의 유효기간이 만료된 경우 발생하는 예외
        } catch (UnsupportedJwtException ex) {
            // Unsupported JWT token: 지원하지 않는 JWT 토큰일 때 발생하는 예외
        } catch (IllegalArgumentException ex) {
            // JWT claims string is empty: JWT 토큰이 비어있을 때 발생하는 예외
        }
        return false;
    }
}

