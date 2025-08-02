package com.example.demo.dto.user;

import com.example.demo.oauth.entity.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDTO {
    private Integer id;
    private String nickname;
    private ProviderType providerType;
    private String email;
    private String profileImage;
}
