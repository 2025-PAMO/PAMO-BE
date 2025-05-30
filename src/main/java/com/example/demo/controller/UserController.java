package com.example.demo.controller;

import com.example.demo.dto.user.UserDto;
import com.example.demo.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mypage")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public UserDto getProfile(@RequestParam Integer id) {
        return userService.getUserProfile(id);
    }

    @PutMapping("/profile")
    public UserDto updateProfile(@RequestParam Integer id, @RequestBody UserDto dto) {
        return userService.updateUserProfile(id, dto);
    }

    @DeleteMapping("/account")
    public void deleteAccount(@RequestParam Integer id) {
        userService.deleteUser(id);
    }
}
