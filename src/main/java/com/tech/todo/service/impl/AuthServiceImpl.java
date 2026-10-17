package com.tech.todo.service.impl;

import com.tech.todo.dto.LoginDto;
import com.tech.todo.dto.RegisterDto;
import com.tech.todo.entity.Role;
import com.tech.todo.entity.User;
import com.tech.todo.exception.TodoApiException;
import com.tech.todo.repository.RoleRepository;
import com.tech.todo.repository.UserRepository;
import com.tech.todo.service.AuthService;
import com.tech.todo.service.SubscriptionService;
import java.util.HashSet;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private SubscriptionService subscriptionService;

    @Override
    public String register(RegisterDto registerDto) {

        subscriptionService.assertCanRegisterUser();

        if (userRepository.existsByUsername(registerDto.getUsername())) {
            throw new TodoApiException(HttpStatus.BAD_REQUEST, "Username already exists.");
        }

        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new TodoApiException(HttpStatus.BAD_REQUEST, "Email already exists.");
        }

        Role userRole = roleRepository.findByName("ROLE_USER");
        if (userRole == null) {
            throw new TodoApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                "ROLE_USER is missing in tenant DB. Ensure Flyway V2/V3 seeded roles.");
        }

        User user = new User();
        user.setName(registerDto.getName());
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);
        return "User registered successfully.";
    }

    @Override
    public String login(LoginDto loginDto) {

        Authentication authenticate = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginDto.getUsernameOrEmail(),
                loginDto.getPassword()));

        //Store Authentication object in SecurityContext - Spring Security uses this
        SecurityContextHolder.getContext().setAuthentication(authenticate);

        return "User logged in successfully.";
    }
}
