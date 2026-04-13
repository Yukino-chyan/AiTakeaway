package com.aitakeaway.server.service;

import com.aitakeaway.server.dto.LoginRequest;
import com.aitakeaway.server.dto.LoginResponse;
import com.aitakeaway.server.dto.RegisterRequest;
import com.aitakeaway.server.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {

    LoginResponse login(LoginRequest request);

    void register(RegisterRequest request);
}
