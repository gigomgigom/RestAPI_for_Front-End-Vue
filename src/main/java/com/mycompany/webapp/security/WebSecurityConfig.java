package com.mycompany.webapp.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class WebSecurityConfig {
	
	@Autowired
	JwtAuthenticationFilter jwtAuthenticationFilter;
	
	//인증 필터 체인 설정
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		//RestApi에서 로그인폼을 제공하지 않으므로 폼을 통한 로그인 인증을 하지 않는다.
		//로그인 폼은 front-end에서 제공
		http.formLogin(config -> config.disable());

		//RestApi에서는 폼 데이터를 받지도 않아서 csrf토큰(사이트간 요청위조방지)을 발급할 필요가 없고
		http.csrf(config -> config.disable()); //csrf 비활성화 (GET 방식 이외의 요청은 _csrf 토큰을 요구하기 때문이다)
		//로그아웃도 세션방식 인증관리가 아니기때문에 로그아웃 설정을 할 필요가 없다.
		
		//세션과 관련된 모든 작업들을 막기 위해서 HttpSession을 비활성화시킨다.
		http.sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		
		//CORS 설정(다른 도메인에서 받은 인증 정보(AccessToken)로 요청할 경우 허가)
		http.cors(config -> {});

		//JWT로 인증이 되도록 필터를 등록(ID, PW를 확인하는 필터 앞에 jwt확인 필터를 두어야한다.)
		http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		
		return http.build();
	}
	
	//인증 관리자을 관리 객체로 등록
	//사용하지 않음. 유저 정보에 접근하기에는 너무 제한적이다. (추후 공부해봐야함)
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
	
	//권한 계층을 관리 객체로 등록
	@Bean
	public RoleHierarchy roleHierarchy() {
		RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
		hierarchy.setHierarchy("ROLE_ADMIN > ROLE_MANAGER > ROLE_USER");
		return hierarchy;
	}
	
	//@PreAuthorize 어노테이션의 표현식을 해석하는 객체 등록
	@Bean
    public MethodSecurityExpressionHandler createExpressionHandler() {
      DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy());
        return handler;
    }
	
	//다른(크로스) 도메인 제한 설정
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		//요청 사이트 제한
		configuration.addAllowedOrigin("*");
		//요청 방식 제한
		configuration.addAllowedMethod("*");
		//요청 헤더 제한
		configuration.addAllowedHeader("*");
		//모든 URL에 대해 위 설정을 내용 적용
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
