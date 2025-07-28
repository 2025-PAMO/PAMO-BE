package com.example.demo.oauth.service;

import com.example.demo.domain.User;
import com.example.demo.oauth.entity.ProviderType;
import com.example.demo.oauth.entity.RoleType;
import com.example.demo.oauth.entity.UserPrincipal;
import com.example.demo.oauth.exception.OAuthProviderMissMatchException;
import com.example.demo.oauth.info.OAuth2UserInfo;
import com.example.demo.oauth.info.OAuth2UserInfoFactory;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        ProviderType providerType = ProviderType.valueOf(
                userRequest.getClientRegistration().getRegistrationId().toUpperCase()
        );

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(providerType, oAuth2User.getAttributes());

        if (userInfo.getId() == null) {
            throw new IllegalArgumentException("OAuth2 provider에서 ID를 찾을 수 없습니다.");
        }

        User user = userRepository.findByUserId(userInfo.getId());

        if (user != null) {
            if (user.getProviderType() != providerType) {
                throw new OAuthProviderMissMatchException("로그인 제공자가 일치하지 않습니다.");
            }
            updateUser(user, userInfo);
        } else {
            user = createUser(userInfo, providerType);
        }

        return UserPrincipal.create(user, oAuth2User.getAttributes());
    }

    private User createUser(OAuth2UserInfo userInfo, ProviderType providerType) {
        User user = new User();
        user.setUserId(userInfo.getId());
        user.setEmail(userInfo.getEmail());
        user.setNickname(userInfo.getName());
        user.setProfileImage(userInfo.getImageUrl());
        user.setProviderType(providerType);
        user.setRoleType(RoleType.USER);
        user.setEmailVerifiedYn("Y");
        user.setCreatedAt(LocalDateTime.now());
        user.setModifiedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    private void updateUser(User user, OAuth2UserInfo userInfo) {
        boolean changed = false;

        if (userInfo.getName() != null && !userInfo.getName().equals(user.getNickname())) {
            user.setNickname(userInfo.getName());
            changed = true;
        }

        if (userInfo.getImageUrl() != null && !userInfo.getImageUrl().equals(user.getProfileImage())) {
            user.setProfileImage(userInfo.getImageUrl());
            changed = true;
        }

        if (changed) {
            user.setModifiedAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }
}
