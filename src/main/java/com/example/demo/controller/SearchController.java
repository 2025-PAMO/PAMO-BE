package com.example.demo.controller;

import com.example.demo.domain.MotionMusic;
import com.example.demo.apiPayload.CustomResponse;
import com.example.demo.dto.search.SearchDtos;
import com.example.demo.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "검색 API", description = "기본/모션 음악을 검색하고 정렬합니다.")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Validated
public class SearchController {

    private final SearchService searchService;

    @Operation(
            summary = "검색 결과 조회",
            description = "검색어와 타입, 정렬 방식을 지정해 모션 음악을 검색하고 결과를 반환합니다."
            ,security = {}
    )
    @GetMapping
    public CustomResponse<SearchDtos.SearchResponse> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "recent") String sort,
            @RequestParam(defaultValue = "10") int limit
    ) {
        SearchDtos.SearchResponse response = searchService.searchMotionMusic(q, sort, limit);
        return CustomResponse.onSuccess(response);
    }
}
