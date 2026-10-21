package com.study.assistant.controller;

import com.study.assistant.dto.*;
import com.study.assistant.service.StudyAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/study")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudyController {

    private final StudyAssistantService studyAssistantService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadMaterial(@RequestParam("file") MultipartFile file) throws IOException {
        studyAssistantService.ingestMaterial(file);
        return ResponseEntity.ok("Material ingested successfully.");
    }

    @PostMapping("/generate-quiz")
    public ResponseEntity<List<QuizResponse>> generateQuiz(@RequestBody QuizGenerationRequest request) {
        return ResponseEntity.ok(studyAssistantService.generateQuiz(request));
    }

    @PostMapping("/ask")
    public ResponseEntity<ChatResponse> askQuestion(@RequestBody ChatRequest request) {
        return ResponseEntity.ok(studyAssistantService.answerQuestion(request.getQuestion()));
    }

    @GetMapping("/documents")
    public ResponseEntity<List<DocumentMetadata>> listDocuments() {
        return ResponseEntity.ok(studyAssistantService.listDocuments());
    }

    @DeleteMapping("/documents/{id}")
    public ResponseEntity<String> deleteDocument(@PathVariable String id) {
        studyAssistantService.deleteDocument(id);
        return ResponseEntity.ok("Document deleted successfully.");
    }
}
