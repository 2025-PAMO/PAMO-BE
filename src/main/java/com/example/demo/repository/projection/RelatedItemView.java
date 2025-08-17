package com.example.demo.repository.projection;

public interface RelatedItemView {
    Integer getId();
    String  getTitle();
    String  getArtist();        // users.nickname
    String  getCoverImageUrl(); // motion_music.cover
    Integer getViewCount();     // motion_music.count (Integer)
}
