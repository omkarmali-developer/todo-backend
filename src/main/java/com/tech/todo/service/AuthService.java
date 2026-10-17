package com.tech.todo.service;

import com.tech.todo.dto.LoginDto;
import com.tech.todo.dto.RegisterDto;

public interface AuthService {

    String register(RegisterDto registerDto);

    String login(LoginDto loginDto);

}
