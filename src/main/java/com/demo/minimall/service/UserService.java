package com.demo.minimall.service;

import com.demo.minimall.dto.LoginDTO;
import com.demo.minimall.entity.User;

public interface UserService {
    String login(LoginDTO dto);
    User getById(Integer id);
    User getByToken(String token);
    void logout(String token);

}
