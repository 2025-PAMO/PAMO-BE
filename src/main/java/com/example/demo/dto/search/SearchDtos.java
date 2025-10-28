package com.example.demo.dto.search;

import lombok.Builder;
import lombok.Data;

import java.util.List;

public class SearchDtos {

    @Data
    @Builder
    public static class SearchItem {
        private Integer id;
        private String title;
        private String coverUrl;
        private Integer playCount;
        private String nickname;
    }

    @Data
    @Builder
    public static class SearchResponse {
        private String query;
        private List<SearchItem> motion;
        private Integer totalMotion;
    }
}
