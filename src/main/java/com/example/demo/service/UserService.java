package com.example.demo.service;

import com.example.demo.apiPayload.code.GeneralErrorCode;
import com.example.demo.apiPayload.exception.CustomException;
import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public void updateProfileImage(Integer userId, MultipartFile profileImage) throws IOException {
        User user = getUserOrThrow(userId);
        String imageUrl = s3Uploader.upload(profileImage, "profile");
        user.setProfileImage(imageUrl);
    }

    @Transactional
    public void updateNickname (Integer userId, String nickname) {
        User user = getUserOrThrow(userId);
        user.setNickname(nickname);
    }

    public User getUserOrThrow(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(GeneralErrorCode.USER_NOT_FOUND));
    }

}
