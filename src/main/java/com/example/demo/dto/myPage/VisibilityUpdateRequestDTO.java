package com.example.demo.dto.myPage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VisibilityUpdateRequestDTO {
    private Boolean visibility;
}
