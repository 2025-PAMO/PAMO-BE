package com.example.demo.dto.music;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // null 필드 응답에서 제외 (explore/mypage 분기용)
public class MusicDetailResponseDTO {

    private com.example.demo.dto.user.UserProfileDTO viewerProfile;
    private String viewerProfileImage;
    private MusicLite music;
    private BaseMusicLite usedBaseMusic;
    private ViewerState viewerState;

    // context=explore 에서만 채움
    private CreatorsUsingBase creatorsUsingBase;
    private List<RelatedLite> related;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MusicLite {
        private Integer id;
        private String title;
        private String artist;
        private String artistProfileImage;
        private String coverImageUrl;
        private String fileUrl;
        private Integer viewCount;
        private Integer likeCount;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BaseMusicLite {
        private Integer id;
        private String artist;
        private String title;
        private String fileUrl;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ViewerState {
        @JsonProperty("isOwner")
        private boolean isOwner;

        @JsonProperty("isLiked")
        private boolean isLiked;

        @JsonProperty("isBookmarkedBaseMusic")
        private boolean isBookmarkedBaseMusic;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreatorsUsingBase {
        private List<String> profileImages; // 최대 3개
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RelatedLite {
        private Integer id;
        private String title;
        private String artist;
        private String coverImageUrl;
        private Integer viewCount;
    }
}
