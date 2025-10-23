package com.example.demo.controller;

import com.example.demo.dto.search.SearchDtos.SearchRequest;
import com.example.demo.dto.search.SearchDtos.SearchResponse;
import com.example.demo.apiPayload.CustomResponse;
import com.example.demo.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "검색 API", description = "기본/모션 음악을 검색하고 정렬합니다.")
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Validated
public class SearchController {

    private final SearchService searchService;

    @Operation(
            summary = "검색 결과 조회",
            description = "검색어와 타입, 정렬 방식을 지정해 기본/모션 음악을 검색하고 결과를 반환합니다."
            ,security = {}
    )
    @GetMapping
    public CustomResponse<SearchResponse> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "all") String type,           //all | basic | motion
            @RequestParam(defaultValue = "relevance") String sort,     // relevance | recent | popular
            @RequestParam(required = false) Integer limit              // null가능 -> 현재는 10개로 해둠
    ) {
        SearchRequest req = SearchRequest.builder()
                .q(q)
                .type(type)
                .sort(sort)
                .limit(limit)
                .build();
        SearchResponse response = searchService.search(req);

        return CustomResponse.onSuccess(response);
    }
}
