package com.example.demo.converter;

import com.example.demo.domain.User;
import com.example.demo.dto.UserDto;

public class UserConverter {
    public static UserDto toDto(User entity) {
        UserDto dto = new UserDto();
        dto.setId(entity.getId());
        dto.setNickname(entity.getNickname());
        dto.setProfileImage(entity.getProfileImage());
        return dto;
    }

    public static void updateEntity(User entity, UserDto dto) {
        entity.setNickname(dto.getNickname());
        entity.setProfileImage(dto.getProfileImage());
    }
}
