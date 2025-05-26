package com.example.demo.dto;

import com.example.demo.domain.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GPTRequestDto {
    private String model;
    private List<Message> messages;
}
