package com.kosta.somacom.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.kosta.somacom.domain.user.User;
import com.kosta.somacom.domain.user.UserStatus;
import com.kosta.somacom.repository.UserRepository;


// security 설정에서 loginProcessingUrl("/loginProc")
// /loginProc 요청이 오면 자동으로 UserDetailsService의 타입으로 IoC 되어있는 loadUserByUsername 함수가 호출된다.
@Service
public class PrincipalDetailsService implements UserDetailsService {

	@Autowired
	private UserRepository userRepository;
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		// Spring Security에서 전달된 username은 우리 시스템의 email에 해당합니다.
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
		if (user.getStatus() == UserStatus.DEACTIVATED) {
            throw new DisabledException("비활성화된 계정입니다.");
        }
		return new PrincipalDetails(user);
	}

}
