package com.study.assistant.dto;

import lombok.Data;

@Data
public class QuizGenerationRequest {
    private String targetTopic;
    private int numberOfQuestions = 5;
}
