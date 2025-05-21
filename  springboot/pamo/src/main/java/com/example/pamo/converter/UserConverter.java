package com.example.pamo.converter;

import com.example.pamo.domain.UserEntity;
import com.example.pamo.dto.UserDto;

public class UserConverter {
    public static UserDto toDto(UserEntity entity) {
        UserDto dto = new UserDto();
        dto.setId(entity.getId());
        dto.setNickname(entity.getNickname());
        dto.setProfileImage(entity.getProfileImage());
        return dto;
    }

    public static void updateEntity(UserEntity entity, UserDto dto) {
        entity.setNickname(dto.getNickname());
        entity.setProfileImage(dto.getProfileImage());
    }
}
