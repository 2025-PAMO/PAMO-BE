package com.example.demo.domain;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@Entity
@Table(name = "music_summary")
public class MusicSummary {

    @Id
    @Column(name = "session_id", length = 64)
    private String sessionId;

    @Column(name = "summary_text", columnDefinition = "TEXT", nullable = false)
    private String summaryText;

    @Column(name = "created_at", insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Timestamp createdAt;

    @OneToOne
    @JoinColumn(name = "base_music_id", insertable = false, updatable = false)
    private BaseMusic baseMusic;

}
