package com.demo.minimall.config;

import com.demo.minimall.entity.User;
import com.demo.minimall.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
@Component
public class LoginInterceptor implements HandlerInterceptor {
    private final UserService userService;

    public LoginInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String url = request.getRequestURI();
        if("GET".equalsIgnoreCase(request.getMethod())&&url.startsWith("/products")){
            return true;
        }
        String token =request.getHeader("Authorization");
        if(token==null||token.isBlank()){
            return reject(response,"未登录");
        }
        if(token.startsWith("Bearer ")){
            token=token.substring(7);
        }
        User user=userService.getByToken(token);
        if(user==null){
            return reject(response,"登录已过期");
        }
        request.setAttribute("currentUser",user);
        return true;
    }
    private boolean reject(HttpServletResponse response,String msg)throws IOException {
        response.setStatus(401);
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().write("{\"code\":401,\"msg\":\"" + msg + "\",\"data\":null}");
        return false;
    }
}
