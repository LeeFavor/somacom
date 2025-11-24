package com.kosta.somacom.config.jwt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kosta.somacom.auth.PrincipalDetails;
import com.kosta.somacom.domain.user.User;
import com.kosta.somacom.repository.UserRepository;

//인가 : 로그인 처리가 되어야만 하는 처리가 들어왔을때 실행
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {

	private UserRepository userRepository;
	public JwtAuthorizationFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
		super(authenticationManager);
		this.userRepository = userRepository;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String authenticationHeader = request.getHeader(JwtProperties.HEADER_STRING);

		// Authorization 헤더가 없거나, Bearer 토큰이 아니면 토큰 검사 없이 다음 필터로 넘긴다.
		// permitAll()로 설정된 API는 이 필터를 통과한 후 Spring Security에 의해 접근이 허용된다.
		if (authenticationHeader == null) {
			chain.doFilter(request, response);
			return;
		}

		//json 형태의 문자열을 map으로 변환
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String,String> token = objectMapper.readValue(authenticationHeader, Map.class);
		
		//access_token : header로부터 accessToken 가져와 bear check
		String accessToken = token.get("access_token");
		if(!accessToken.startsWith(JwtProperties.TOKEN_PREFIX)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인 필요");
			return;
		}
		//access token에서 Bearer 삭제
		accessToken = accessToken.replace(JwtProperties.TOKEN_PREFIX, "");
		
		try {
			//1. access token check
			//1-1. 보안키, 만료시간 체크
			String email = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET))
									.build()
									.verify(accessToken)
									.getClaim("sub")
									.asString();
			System.out.println("Email from token: " + email);
			if(email == null || email.isEmpty()) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인 필요");
				return;
			}
			//1-2. email로 사용자 조회
			Optional<User> ouser = userRepository.findByEmail(email);
			if(ouser.isEmpty()) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "존재하지 않는 사용자입니다.");
				return;
			}
			
			PrincipalDetails principalDetails = new PrincipalDetails(ouser.get());
			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principalDetails,null,
					principalDetails.getAuthorities());
			SecurityContextHolder.getContext().setAuthentication(auth);
			chain.doFilter(request, response);
			return;
			
		} catch(TokenExpiredException e) { //access token이 기간 만료되었을때 refresh token check 함
			e.printStackTrace();
			//1. refresh token 타당할 경우
			String refreshToken = token.get("refresh_token");

			if(!refreshToken.startsWith(JwtProperties.TOKEN_PREFIX)) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인 필요");
				return;				
			}
			
			refreshToken = refreshToken.replace(JwtProperties.TOKEN_PREFIX, "");
			
			try {
				String email = JWT.require(Algorithm.HMAC512(JwtProperties.SECRET))
									.build()
									.verify(refreshToken)
									.getClaim("sub")
									.asString();
				if(email == null || email.isEmpty()) {
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인 필요");
					return;				
				}
				
				Optional<User> ouser =  userRepository.findByEmail(email);
				if(ouser.isEmpty()) {
					response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "존재하지 않는 사용자입니다.");
					return;									
				}
				
				//새 토큰 생성
				JwtToken jwtToken = new JwtToken();
				String nAccessToken = jwtToken.makeAccessToken(email);
				String nRefreshToken = jwtToken.makeRefreshToken(email);
				
				Map<String,String> mtoken = new HashMap<>();
				mtoken.put("access_token", JwtProperties.TOKEN_PREFIX+nAccessToken);
				mtoken.put("refresh_token", JwtProperties.TOKEN_PREFIX+nRefreshToken);
				
				String nToken = objectMapper.writeValueAsString(mtoken);
				
				//response header에 새로 만든 토큰을 넣어준다.
				response.addHeader(JwtProperties.HEADER_STRING, nToken);
				
				PrincipalDetails principalDetails = new PrincipalDetails(ouser.get());
				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principalDetails,null,
						principalDetails.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(auth);				
				chain.doFilter(request, response);
				return;
				
			} catch(TokenExpiredException re) {  //2. refresh token 기간 만료
				re.printStackTrace();
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인 필요");
				return;				
			}
		} catch(Exception e) {
			e.printStackTrace();
		}		
	}
}
