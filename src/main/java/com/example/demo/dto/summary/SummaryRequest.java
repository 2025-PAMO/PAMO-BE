package com.example.demo.dto.summary;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SummaryRequest {
    private String sessionId;
    private String summaryText;
}
