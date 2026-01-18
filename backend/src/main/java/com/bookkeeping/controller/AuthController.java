package com.bookkeeping.controller;

import com.bookkeeping.entity.User;
import com.bookkeeping.service.UserService;
import com.bookkeeping.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<User> register(@RequestBody Map<String, String> payload) {
        try {
            User user = userService.register(payload.get("username"), payload.get("password"));
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<User> login(@RequestBody Map<String, String> payload) {
        try {
            User user = userService.login(payload.get("username"), payload.get("password"));
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(401, e.getMessage());
        }
    }
}
