package com.kosta.somacom.config.jwt;

public interface JwtProperties {
	String SECRET="코스타";
	Integer ACCESS_EXPIRATION_TIME = 60000*60;
	Integer REFRESH_EXPIRATION_TIME = 60000*60*24*14;
	String TOKEN_PREFIX = "Bearer ";
	String HEADER_STRING = "Authorization";
}
