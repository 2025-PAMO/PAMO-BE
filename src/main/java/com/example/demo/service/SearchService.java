package com.example.demo.service;

import com.example.demo.converter.SearchConverter;
import com.example.demo.domain.MotionMusic;
import com.example.demo.dto.search.SearchDtos;
import com.example.demo.repository.BaseMusicRepository;
import com.example.demo.repository.MotionMusicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    //최대 검색 아이템 개수는 현재 10개로 정해둠 -> 추후 늘리기
    private static final int DEFAULT_LIMIT = 10;

    private final MotionMusicRepository motionMusicRepository;

    public SearchDtos.SearchResponse searchMotionMusic(String q, String sort, int limit) {
        String query = q == null ? "" : q.trim();

        Pageable pageable = PageRequest.of(0, Math.max(1, limit));
        List<MotionMusic> results = motionMusicRepository.searchMotion(query, sort, pageable);

        return SearchConverter.buildResponse(query, results);}

}
