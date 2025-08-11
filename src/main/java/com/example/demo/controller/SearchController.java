package com.example.demo.controller;

import com.example.demo.dto.search.SearchDtos.SearchRequest;
import com.example.demo.dto.search.SearchDtos.SearchResponse;
import com.example.demo.apiPayload.CustomResponse;
import com.example.demo.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Validated
public class SearchController {

    private final SearchService searchService;

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
