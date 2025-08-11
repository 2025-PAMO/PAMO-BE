package com.example.demo.repository.projection;

public interface MusicSearchProjection {
    Integer getId();
    String getTitle();
    String getCover();
    Long getLikeCount();
    Long getPlayCount();   // 재생 수의 경우 BaseMusic은 null로 들어가도록 함
}
