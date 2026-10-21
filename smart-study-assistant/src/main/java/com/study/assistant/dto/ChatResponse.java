package com.study.assistant.dto;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ChatResponse {
    private String answer;
    private List<String> sources;
}
