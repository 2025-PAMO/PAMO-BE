package com.example.demo.service;

import com.example.demo.converter.UserConverter;
import com.example.demo.domain.User;
import com.example.demo.dto.UserDto;
import com.example.demo.exception.code.UserErrorCode;
import com.example.demo.exception.handler.UserException;
import com.example.demo.persistence.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto getUserProfile(Integer id) {
        User entity = userRepository.findById(id)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        return UserConverter.toDto(entity);
    }

    public UserDto updateUserProfile(Integer id, UserDto dto) {
        User entity = userRepository.findById(id)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        UserConverter.updateEntity(entity, dto);
        userRepository.save(entity);
        return UserConverter.toDto(entity);
    }

    public void deleteUser(Integer id) {
        userRepository.findById(id)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        userRepository.deleteById(id);
    }
}
