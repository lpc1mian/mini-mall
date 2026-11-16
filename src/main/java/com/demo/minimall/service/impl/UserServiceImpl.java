package com.demo.minimall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demo.minimall.common.BusinessException;
import com.demo.minimall.config.TokenStore;
import com.demo.minimall.mapper.UserMapper;
import com.demo.minimall.dto.LoginDTO;
import com.demo.minimall.entity.User;
import com.demo.minimall.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final TokenStore tokenStore;

    public UserServiceImpl(UserMapper userMapper, TokenStore tokenStore) {
        this.userMapper = userMapper;
        this.tokenStore = tokenStore;
    }
    @Override
    public String login(LoginDTO dto){
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if(user == null||!dto.getPassword().equals(user.getPassword())){
            throw new BusinessException("用户名或密码错误");
        }
        return tokenStore.save(user.getId());
    }
    @Override
    public User getById(Integer id){
        User user = userMapper.selectById(id);
        if(user == null){
            return null;
        }
        user.setPassword(null);
        return user;
    }
    @Override
    public User getByToken(String token){
        Integer userId = tokenStore.get(token);
        if(userId == null){
            return null;
        }
        User user = userMapper.selectById(userId);
        if(user == null){
            return null;
        }
        user.setPassword(null);
        return user;
    }
    @Override
    public void logout(String token){
        tokenStore.remove(token);
    }


}
