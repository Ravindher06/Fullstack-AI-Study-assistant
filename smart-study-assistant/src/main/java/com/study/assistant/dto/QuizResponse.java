package com.study.assistant.dto;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class QuizResponse {
    private int questionNumber;
    private String questionText;
    private List<String> multipleChoiceOptions;
    private String correctAnswer;
}
