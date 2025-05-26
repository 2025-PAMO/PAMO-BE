package com.example.demo.dto.chat;

import lombok.Data;

@Data
public class ChatRequestDTO {
    private Integer userId;
    private String question;
}
