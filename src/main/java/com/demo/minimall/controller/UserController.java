package com.demo.minimall.controller;

import com.demo.minimall.dto.LoginDTO;
import com.demo.minimall.common.Result;
import com.demo.minimall.entity.User;
import com.demo.minimall.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;


    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Integer id) {
        return Result.success(userService.getById(id));
    }
    @PostMapping("/login")
    public Result<String> login(@RequestBody @Valid LoginDTO dto) {
        return Result.success(userService.login(dto));
    }
    @GetMapping("/me")
    public Result<User> me(@RequestAttribute("currentUser") User user) {
        return Result.success(user);
    }
    @PostMapping("/logout")
    public Result<String> logout(@RequestHeader(value = "Authorization",required = false) String token)
    {
        if(token!=null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        userService.logout(token);
        return Result.success("登出成功");
    }
}
