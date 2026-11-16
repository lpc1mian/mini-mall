package com.demo.minimall.config;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenStore {
    private final Map<String, Integer> tokenStore =new ConcurrentHashMap<>();
    public String save(Integer userId){
        String token=UUID.randomUUID().toString().replace("-", "");
        tokenStore.put(token, userId);
        return token;
    }
    public Integer get(String token){
        if(token==null){
            return null;
        }
        return tokenStore.get(token);//这个方法返回的是用户ID，为什么
    }
    public void remove(String token){
        if(token==null){
            return;
        }
        tokenStore.remove(token);
    }

}
