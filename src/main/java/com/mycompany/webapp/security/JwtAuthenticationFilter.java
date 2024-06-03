package com.mycompany.webapp.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter{
	
	@Autowired
	private JwtProvider jwtProvider;
	
	@Autowired
	private AppUserDetailsService userDetailsService;
	
	//인증 필터 체인을 관리 객체로 등록
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		//AccessToken얻기
		String accessToken = null;
		HttpServletRequest httpServletRequest = (HttpServletRequest) request;
		//HTTP 요청 헤더값에 Authorization의 값을 받아온다.
		String headerValue = httpServletRequest.getHeader("Authorization");
		// headerValue값이 null이 아니고 Bearer접두사가 붙어있느냐?
		if(headerValue != null && headerValue.startsWith("Bearer")) {
			//Bearer 접두사 이후의 AccessToken값을 받아온다.
			accessToken = headerValue.substring(7);
			log.info(accessToken);
		}
		
		if(accessToken != null) {
			//AccessToken 유효성 검사
			Jws<Claims> jws = jwtProvider.validateToken(accessToken);
			if(jws != null) {
				//유효한 경우
				log.info("AccessToken이 유효함");
				String mid = jwtProvider.getUserId(jws);
				log.info(mid);
				//사용자 상세정보 얻기
				AppUserDetails userDetails = (AppUserDetails) userDetailsService.loadUserByUsername(mid);
				//인증객체 얻기
				Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
				//스프링 시큐리티에 인증 객체 설정
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} else {
				//유효하지 않은 경우
				log.info("AccessToken이 유효하지 않음");
			}
		}
		//다음 필터를 실행
		filterChain.doFilter(request, response);
		
	}
	
}
