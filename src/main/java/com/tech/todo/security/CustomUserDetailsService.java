package com.tech.todo.security;

import com.tech.todo.entity.User;
import com.tech.todo.repository.UserRepository;
import com.tech.todo.tenant.TenantContext;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Loads users from the current tenant DB only — never from {@code todo_master}.
 */
@Service("tenantUserDetailsService")
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
		if (TenantContext.getTenantKey() == null || TenantContext.getTenantKey().isBlank()) {
			throw new UsernameNotFoundException(
					"X-Tenant-Id is required to authenticate against a tenant database");
		}

		User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
				.orElseThrow(() -> new UsernameNotFoundException(
						"User not found with username or email: " + usernameOrEmail));

		Set<SimpleGrantedAuthority> authorities = user.getRoles() == null
				? Set.of()
				: user.getRoles().stream()
						.filter(role -> role.getName() != null && !role.getName().isBlank())
						.map(role -> new SimpleGrantedAuthority(role.getName()))
						.collect(Collectors.toSet());

		return new org.springframework.security.core.userdetails.User(
				user.getUsername(),
				user.getPassword(),
				authorities);
	}
}
