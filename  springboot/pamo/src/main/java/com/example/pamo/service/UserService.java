package com.example.pamo.service;

import com.example.pamo.converter.UserConverter;
import com.example.pamo.domain.UserEntity;
import com.example.pamo.dto.UserDto;
import com.example.pamo.exception.code.UserErrorCode;
import com.example.pamo.exception.handler.UserException;
import com.example.pamo.persistence.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto getUserProfile(Integer id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        return UserConverter.toDto(entity);
    }

    public UserDto updateUserProfile(Integer id, UserDto dto) {
        UserEntity entity = userRepository.findById(id)
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
