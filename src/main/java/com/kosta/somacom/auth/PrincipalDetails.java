package com.kosta.somacom.auth;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.kosta.somacom.domain.user.User;

import lombok.Data;
// security가 /loginProc 주소를 낚아채서 로그인 한다.
// 로그인 진행이 완료가 되면 security session을 만들어 준다(Security ContextHolder)
// security session에 들어가는 타입은 Authentication 타입의 객체여야 한다.
// 그래서, Authentication 안에 User 정보를 넣어야 한다.
// 그 User 오브젝트 타입은 UserDetails 타입이어야 한다.
// 즉, (Security ContextHoder (new Authentication(new UserDetails(new User)))
@Data
public class PrincipalDetails implements UserDetails {
	private User user;
	public PrincipalDetails(User user) {
		this.user=user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		// UserRole Enum을 Spring Security가 인식할 수 있는 GrantedAuthority 형태로 변환합니다.
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(() -> "ROLE_" + user.getRole().name());
		return authorities;
	}

	@Override
	public String getPassword() {
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		// Spring Security에서 'username'은 고유 식별자(ID)를 의미합니다.
		// 우리 시스템에서는 email을 ID로 사용하므로 user.getEmail()을 반환합니다.
		return user.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		//우리 사이트에서 1년동안 로그인을 안하면 휴먼 계정으로 변하기로 했다면
		//현재시간 - 마지막 로그인한 시간을 계산하여 1년 초과하면 return false;
		return true;
	}

}
