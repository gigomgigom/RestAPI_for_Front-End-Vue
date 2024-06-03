package com.mycompany.webapp.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtProvider {
	
	//AccessToken의 유효 기간(단위: milisecond)
	private long accessTokenDuration = 24*60*60*1000;
	//서명 및 암호화를 위한 SecretKey 생성
	private SecretKey secretKey;
	
	//생성자
	public JwtProvider(@Value("${jwt.security.key}") String jwtSecurityKey) {
		
		log.info(jwtSecurityKey);
		try {
			//application.property에서 문자열 키를 읽고, SecretKey를 생성한다.
			secretKey = Keys.hmacShaKeyFor(jwtSecurityKey.getBytes("UTF-8"));
		} catch (Exception e) {
			log.info(e.toString());
		} 
	}
	
	//AccessToken 생성
	public String createAccessToken(String userId, String authority) {
		String token = null;
		try {
			JwtBuilder builder = Jwts.builder();
			//header 설정
			//자동으로 설정
			
			//payload 설정
			builder.subject(userId);
			builder.claim("authority", authority);
			builder.expiration(new Date(new Date().getTime() + accessTokenDuration));
			
			//signature 설정
			builder.signWith(secretKey);
			token = builder.compact();
		} catch(Exception e) {
			log.info(e.toString());
		}
		return token;
	}
	
	public Jws<Claims> validateToken(String accessToken) {
		Jws<Claims> jws = null;
        try {
        	//JWT 파서 빌더 생성
            JwtParserBuilder builder = Jwts.parser();
            //JWT 파서 빌더에 비밀키 설정
            builder.verifyWith(secretKey);
            //JWT 파서 생성
            JwtParser parser = builder.build();
            //AccessToken으로부터 payload 얻기
            jws = parser.parseSignedClaims(accessToken);
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
        	log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
        	log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
        	log.info("JWT 토큰이 잘못되었습니다.");
        }
        return jws;
    }
	
    public String getUserId(Jws<Claims> jws) {
    	//Payload 얻기
        Claims claims = jws.getPayload();
        //사용자 아이디 얻기
        String userId = claims.getSubject();
        return userId;
    }
    
    public String getAuthority(Jws<Claims> jws) {
    	//Payload 얻기
        Claims claims = jws.getPayload();
    	//사용자 권한 얻기
    	String autority = claims.get("authority").toString();
        return autority;
    }	
	
    
    /*
    //Test 용도로 사용하기 (Run As - Java Application)
	public static final void main(String[] args) {
		JwtProvider jwtProvider = new JwtProvider("com.mycompany.jsonwebtoken.kosacourse");
		
		String accessToken = jwtProvider.createAccessToken("user", "ROLE_USER");
		log.info("AccessToken: " + accessToken);
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		
		Jws<Claims> jws = jwtProvider.validateToken(accessToken);
		log.info("validate: " + ((jws!=null)? true : false));
		
		
		if(jws != null) {
			String userId = jwtProvider.getUserId(jws);
			log.info("userId: " + userId);
			
			String autority = jwtProvider.getAuthority(jws);
			log.info("autority: " + autority); 
		}
	}*/
}






