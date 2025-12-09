package com.kosta.somacom.config.jwt;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.kosta.somacom.auth.PrincipalDetails;
import com.kosta.somacom.auth.dto.LoginRequestDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private final JwtToken jwtToken;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public JwtAuthenticationFilter(AuthenticationManager authenticationManager) {
		super(authenticationManager);
		this.jwtToken = new JwtToken();
	}

	// /api/auth/login 요청을 하면 로그인 시도를 위해서 실행되는 함수
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		System.out.println("JwtAuthenticationFilter: 로그인 시도중");

		try {
			// 1. request body에서 username(email), password를 JSON으로 읽어들인다.
			LoginRequestDto loginRequestDto = objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);

			// 2. 인증 토큰(UsernamePasswordAuthenticationToken) 생성
			UsernamePasswordAuthenticationToken authenticationToken =
					new UsernamePasswordAuthenticationToken(loginRequestDto.getUsername(), loginRequestDto.getPassword());

			// 3. PrincipalDetailsService의 loadUserByUsername() 함수가 실행된 후 정상이면 authentication이 리턴됨.
			// DB에 있는 email과 password가 일치한다는 뜻.
			Authentication authentication = getAuthenticationManager().authenticate(authenticationToken);
			return authentication;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	//super의 attemptAuthentication 메소드가 실행되고 성공하면 successfulAuthentication가 호출된다.
	//attemptAuthentication 메소드가 리턴해준 Authentication을 파라미터로 받음
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {
		PrincipalDetails principalDetails = (PrincipalDetails)authResult.getPrincipal();
		String email = principalDetails.getUsername(); // PrincipalDetails에서 getUsername()은 email을 반환합니다.

		// Access Token과 Refresh Token 생성
		String accessToken = jwtToken.makeAccessToken(email);
		String refreshToken = jwtToken.makeRefreshToken(email);

		// 응답 헤더에 토큰 설정 (JSON 형태)
		Map<String,String> map = new HashMap<>();
		map.put("access_token", JwtProperties.TOKEN_PREFIX+accessToken);
		map.put("refresh_token", JwtProperties.TOKEN_PREFIX+refreshToken);
		String token = objectMapper.writeValueAsString(map);

		response.addHeader(JwtProperties.HEADER_STRING, token);
		response.setContentType("application/json; charset=utf-8");

		// 응답 본문에 사용자 정보 설정
		Map<String,Object> userInfo = new HashMap<>();
		userInfo.put("email", principalDetails.getUser().getEmail());
		userInfo.put("username", principalDetails.getUser().getUsername()); // 닉네임
		userInfo.put("role", principalDetails.getUser().getRole().name());

		response.getWriter().write(objectMapper.writeValueAsString(userInfo));
	}
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
											  AuthenticationException failed) throws IOException, ServletException {
		
		log.error("Login failed: {}", failed.getMessage());

		HttpStatus status = HttpStatus.UNAUTHORIZED; // 401
		String errorMessage = "아이디나 비밀번호를 확인하세요.";

		if (failed instanceof LockedException) {
			status = HttpStatus.FORBIDDEN; // 403
			errorMessage = "관리자에 의해 정지된 계정입니다.";
		} else if (failed instanceof DisabledException) {
			status = HttpStatus.FORBIDDEN; // 403
			errorMessage = "탈퇴 처리된 계정입니다.";
		}
		// 다른 종류의 AuthenticationException에 대한 처리를 추가할 수 있습니다.

		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode json = objectMapper.createObjectNode();
		json.put("status", status.value());
		json.put("error", status.getReasonPhrase());
		json.put("message", errorMessage);
		response.getWriter().write(objectMapper.writeValueAsString(json));
	}
}
