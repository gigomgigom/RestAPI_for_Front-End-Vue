package com.mycompany.webapp.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.webapp.dto.Member;
import com.mycompany.webapp.security.AppUserDetails;
import com.mycompany.webapp.security.AppUserDetailsService;
import com.mycompany.webapp.security.JwtProvider;
import com.mycompany.webapp.service.MemberService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/member")
public class MemberController {
	@Autowired
	private JwtProvider jwtProvider;
	
	@Autowired
	private AppUserDetailsService userDetailsService;
	
	@Autowired
	private MemberService memberService;
	
	@PostMapping("/login")
	public Map<String, String> userLogin(String mid, String mpassword) {
		//사용자 상세 정보 얻기
		AppUserDetails userDetails = (AppUserDetails) userDetailsService.loadUserByUsername(mid);
		
		PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		
		//응답 생성
		Map<String, String> resultMap = new HashMap<>();
		
		//비밀번호 체크를 해서 일치할 경우
		if(passwordEncoder.matches(mpassword, userDetails.getMember().getMpassword())) {
			//인증 객체 생성
			Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, null);
			//스프링 시큐리티에 인증 객체를 추가
			SecurityContextHolder.getContext().setAuthentication(authentication);
			
			//AccessToken 생성
			String accessToken = jwtProvider.createAccessToken(mid, userDetails.getMember().getMrole());
			//JSON 응답 생성
			resultMap.put("result", "success");
			resultMap.put("mid", mid);
			resultMap.put("accessToken", accessToken);
		} else {
			resultMap.put("result", "fail");
		}
		
		return resultMap;
	}
	
	@PostMapping("/join")
	public Member userJoin(@RequestBody Member member) {
		PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
		
		member.setMpassword(passwordEncoder.encode(member.getMpassword()));
		member.setMenabled(true);
		member.setMrole("ROLE_USER");
		
		memberService.join(member);
		
		//JSON 출력 데이터에서 비밀번호 제거
		member.setMpassword(null);
		
		return member;
	}
}
