package com.example.demo.converter;

import com.example.demo.domain.MusicSummary;
import com.example.demo.dto.summary.SummaryDTO;

public class SummaryConverter {

    public static SummaryDTO toDTO(MusicSummary entity) {
        SummaryDTO dto = new SummaryDTO();
        dto.setSessionId(entity.getSessionId());
        dto.setSummaryText(entity.getSummaryText());
        return dto;
    }

    public static MusicSummary toEntity(SummaryDTO dto) {
        MusicSummary entity = new MusicSummary();
        entity.setSessionId(dto.getSessionId());
        entity.setSummaryText(dto.getSummaryText());
        return entity;
    }
}
