package com.example.demo.Mapper;

import com.example.demo.DTO.SummaryDTO;
import com.example.demo.Entity.MusicSummary;

public class SummaryMapper {

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
