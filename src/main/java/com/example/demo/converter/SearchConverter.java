package com.example.demo.converter;

import com.example.demo.dto.search.SearchDtos.SearchItem;
import com.example.demo.dto.search.SearchDtos.SearchResponse;
import com.example.demo.domain.MotionMusic;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class SearchConverter {

    private SearchConverter() {}

    public static SearchItem toItem(MotionMusic m) {
        if (m == null) return null;

        return SearchItem.builder()
                .id(m.getId())
                .title(m.getTitle())
                .coverUrl(m.getCover())
                .playCount(m.getCount())
                .nickname(m.getUser() != null ? m.getUser().getNickname() : null) // ✅ user 정보 일부만
                .build();
    }

    public static List<SearchItem> toItems(List<MotionMusic> list) {
        if (list == null || list.isEmpty()) return List.of();
        return list.stream()
                .filter(Objects::nonNull)
                .map(SearchConverter::toItem)
                .collect(Collectors.toList());
    }

    public static SearchResponse buildResponse(String query, List<MotionMusic> motionMusics) {
        List<SearchItem> items = toItems(motionMusics);
        return SearchResponse.builder()
                .query(query)
                .motion(items)
                .totalMotion(items.size())
                .build();
    }
}

