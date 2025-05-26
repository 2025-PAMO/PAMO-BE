package com.example.demo.dto;

import com.example.demo.domain.Message;
import lombok.Data;

@Data
public class GPTRequestDto {
    private String model;
    private Message message;

    public GPTRequestDto(String model, String prompt) {
        this.model = model;
        this.message = new Message("user", prompt);
    }
}
