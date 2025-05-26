package com.example.demo.dto;

import lombok.Data;

@Data
public class ChatRequestDto {
    private Integer userId;
    private String question;
}
