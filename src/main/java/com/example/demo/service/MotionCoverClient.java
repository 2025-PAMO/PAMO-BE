package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.Map;

@Service
public class MotionCoverClient {

    private final WebClient webClient;
    private final String thumbFromS3Path;

    public MotionCoverClient(
            @Value("${python.base-url}") String baseUrl,
            @Value("${python.endpoints.generate-thumbnail-from-s3}") String thumbFromS3Path
    ) {
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
        this.thumbFromS3Path = thumbFromS3Path;
    }

    public Map<String, Object> generateThumbnailFromS3(
            String inBucket, String inKey, Double timestampSec,
            String outBucket, String outPrefix
    ) {
        var payload = new HashMap<String, Object>();
        payload.put("s3_bucket", inBucket);
        payload.put("s3_key", inKey);
        if (timestampSec != null) payload.put("timestamp_sec", timestampSec);
        payload.put("out_s3_bucket", outBucket);
        payload.put("out_s3_prefix", outPrefix);

        return webClient.post()
                .uri(thumbFromS3Path)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }
}
