package com.example.demo.controller;

import com.example.demo.service.MotionCoverClient;           // ✅ 꼭 필요
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class PresignController {

    private final S3Presigner presigner;
    private final MotionCoverClient motionClient;

    @Value("${app.s3.bucket}") String bucket;
    @Value("${app.s3.uploads-prefix}") String uploadsPrefix;
    @Value("${app.s3.thumbs-prefix}") String thumbsPrefix;

    public PresignController(S3Presigner presigner, MotionCoverClient motionClient) {
        this.presigner = presigner;
        this.motionClient = motionClient;
    }

    @GetMapping("/presign/put-video")
    public Map<String, Object> presignPut(@RequestParam(required = false) String filename) {
        String key = (uploadsPrefix + (filename != null ? filename : (UUID.randomUUID() + ".mp4")))
                .replace("//","/");

        PutObjectRequest putReq = PutObjectRequest.builder()
                .bucket(bucket).key(key).contentType("video/mp4").build();

        PresignedPutObjectRequest presigned = presigner.presignPutObject(b -> b
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(putReq));

        return Map.of(
                "bucket", bucket,
                "key", key,
                "url", presigned.url().toString(),
                "expiresInSeconds", 300
        );
    }

    @PostMapping(path = "/thumbnail/from-s3", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> makeThumbFromS3(@RequestBody MakeThumbReq req) {
        String inBucket = (req.inBucket != null) ? req.inBucket : bucket;
        return motionClient.generateThumbnailFromS3(
                inBucket, req.inKey, req.timestampSec,
                bucket,  // 동일 버킷
                thumbsPrefix
        );
    }

    @Data
    public static class MakeThumbReq {
        String inBucket;     // optional
        String inKey;        // required
        Double timestampSec; // optional (null -> 8.0)
    }
}
