package com.example.demo.dto.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public final class SearchDtos {
    private SearchDtos() {}

    //검색 요청 시 사용
    @Getter
    @Builder
    @AllArgsConstructor @NoArgsConstructor(force = true)
    public static class SearchRequest {
        private final String q;              // 검색어
        private final String type;           // all | basic | motion (기본: all)
        private final String sort;           // relevance | recent | popular (기본: relevance)
        private final Integer limit;         // 최대 몇 개까지 응답할지 (안줘도 됨)
    }

    //조회 시 1개의 아이템 dto
    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(force = true)
    @JsonInclude(JsonInclude.Include.NON_NULL) // 기본음악 재생수와 같은 null필드는 JSON에서 제외하도록 함
    public static class SearchItem {
        private final Integer id;
        private final String title;
        private final String coverUrl;
        private final String kind;
        private final Long likeCount;
        private final Long playCount;
    }

    // 섹션 분리된 단순 응답시 item 반환 리스트
    @Getter
    @Builder
    @AllArgsConstructor @NoArgsConstructor(force = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SearchResponse {
        private final String query;
        private final List<SearchItem> basic;
        private final List<SearchItem> motion;
        private final Integer totalBasic;
        private final Integer totalMotion;
    }
}
