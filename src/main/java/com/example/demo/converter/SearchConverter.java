package com.example.demo.converter;

import com.example.demo.dto.search.SearchDtos.SearchItem;
import com.example.demo.dto.search.SearchDtos.SearchResponse;
import com.example.demo.repository.projection.MusicSearchProjection;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class SearchConverter {

    private SearchConverter() {}

    public static SearchItem toItem(MusicSearchProjection p, String kind) {
        if (p == null) return null;
        return SearchItem.builder()
                .id(p.getId())
                .title(p.getTitle())
                .coverUrl(p.getCover())
                .likeCount(p.getLikeCount())
                .playCount(p.getPlayCount()) // BaseMusic이면 null
                .kind(kind)
                .build();
    }

    public static List<SearchItem> toItems(List<MusicSearchProjection> list, String kind) {
        if (list == null) return List.of();
        return list.stream()
                .map(p -> toItem(p, kind))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static SearchResponse buildResponse(String query, String type,
                                               List<SearchItem> basicItems,
                                               List<SearchItem> motionItems) {
        boolean wantBasic = "all".equalsIgnoreCase(type) || "basic".equalsIgnoreCase(type);
        boolean wantMotion = "all".equalsIgnoreCase(type) || "motion".equalsIgnoreCase(type);

        return SearchResponse.builder()
                .query(query)
                .basic(wantBasic ? basicItems : null)
                .motion(wantMotion ? motionItems : null)
                .totalBasic(wantBasic && basicItems != null ? basicItems.size() : null)
                .totalMotion(wantMotion && motionItems != null ? motionItems.size() : null)
                .build();
    }
}
