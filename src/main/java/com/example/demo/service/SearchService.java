package com.example.demo.service;

import com.example.demo.dto.search.SearchDtos.SearchItem;
import com.example.demo.dto.search.SearchDtos.SearchRequest;
import com.example.demo.dto.search.SearchDtos.SearchResponse;
import com.example.demo.repository.BaseMusicRepository;
import com.example.demo.repository.MotionMusicRepository;
import com.example.demo.repository.projection.MusicSearchProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    //최대 검색 아이템 개수는 현재 10개로 정해둠 -> 추후 늘리기
    private static final int DEFAULT_LIMIT = 10;

    private final BaseMusicRepository baseRepo;
    private final MotionMusicRepository motionRepo;

    public SearchResponse search(SearchRequest request) {
        String qOriginal = request.getQ();
        String q = normalize(qOriginal);
        String type = safeType(request.getType());
        String sort = safeSort(request.getSort());
        int limit = request.getLimit() == null ? DEFAULT_LIMIT : Math.max(1, request.getLimit());

        List<SearchItem> basicItems = null;
        List<SearchItem> motionItems = null;

        boolean wantBasic = "all".equals(type) || "basic".equals(type);
        boolean wantMotion = "all".equals(type) || "motion".equals(type);

        if (wantBasic) {
            basicItems = toSearchItems(baseRepo.searchBase(q, sort, limit), "basic");
        }
        if (wantMotion) {
            motionItems = toSearchItems(motionRepo.searchMotion(q, sort, limit), "motion");
        }

        return SearchResponse.builder()
                .query(qOriginal)
                .basic(basicItems)
                .motion(motionItems)
                .totalBasic(basicItems != null ? basicItems.size() : null)
                .totalMotion(motionItems != null ? motionItems.size() : null)
                .build();
    }

    private List<SearchItem> toSearchItems(List<MusicSearchProjection> projections, String kind) {
        return projections.stream()
                .map(p -> SearchItem.builder()
                        .id(p.getId())
                        .title(p.getTitle())
                        .coverUrl(p.getCover())
                        .likeCount(p.getLikeCount())
                        .playCount(p.getPlayCount()) // BaseMusic이면 null, MotionMusic이면 값 있음
                        .kind(kind)
                        .build())
                .toList();
    }

    private String normalize(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private String safeType(String t) {
        if (t == null) return "all";
        return switch (t.toLowerCase()) {
            case "basic", "motion", "all" -> t.toLowerCase();
            default -> "all";
        };
    }

    private String safeSort(String s) {
        if (s == null) return "relevance";
        return switch (s.toLowerCase()) {
            case "relevance", "recent", "popular" -> s.toLowerCase();
            default -> "relevance";
        };
    }
}
