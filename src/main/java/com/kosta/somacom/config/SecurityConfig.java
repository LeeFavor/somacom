package com.kosta.somacom.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.kosta.somacom.config.jwt.JwtAuthenticationFilter;
import com.kosta.somacom.config.jwt.JwtAuthorizationFilter;
import com.kosta.somacom.repository.UserRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Autowired
	private UserRepository userRepository;
	
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}
	
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
		http.cors() //다른 도메인 접근 허용, @Bean 에 의해 생성된 CorsFilter 자동으로 설정됨
			.and()
			.csrf().disable() //csrf 공격 비활성화
			.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); //session 비활성화
		
		http.formLogin().disable() //로그인 폼 비활성화
			.httpBasic().disable(); //httpBasic은 header에 username, password를 암호화하지 않은 상태로 주고받는다. 이를 사용하지 않겠다는 의미

		JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager);
		jwtAuthenticationFilter.setFilterProcessesUrl("/api/auth/login"); // 로그인 URL 설정

		http.addFilter(jwtAuthenticationFilter) // 로그인 처리를 위한 JwtAuthenticationFilter
			.addFilter(new JwtAuthorizationFilter(authenticationManager, userRepository)) // 토큰 검증을 위한 JwtAuthorizationFilter
			.authorizeRequests()
			// ADMIN 전용
			.antMatchers("/api/admin/**").hasRole("ADMIN")
			// SELLER 전용
			.antMatchers("/api/seller/**").hasRole("SELLER")
			.antMatchers("/api/seller/orders/**").hasRole("SELLER")
			// USER 전용 (장바구니, 주문 등)
			.antMatchers("/api/cart/**").hasRole("USER")
			.antMatchers("/api/orders/**").hasRole("USER")
			// 그 외 모든 요청은 허용 (상품 조회, 회원가입, 로그인 등)
			.anyRequest().permitAll();
		
		return http.build();		
	}
	
	@Bean
	public BCryptPasswordEncoder encoderPassword() {
		return new BCryptPasswordEncoder();
	}
}
