
package com.study.assistant.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.assistant.dto.ChatResponse;
import com.study.assistant.dto.DocumentMetadata;
import com.study.assistant.dto.QuizGenerationRequest;
import com.study.assistant.dto.QuizResponse;

import jakarta.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyAssistantService {

    private final ChatClient.Builder chatClientBuilder;
    private final SimpleVectorStore vectorStore;
    private final ObjectMapper objectMapper;

    @Value("${study.data-dir:./data}")
    private String dataDir;

    private final List<DocumentMetadata> uploadedDocs =
            new CopyOnWriteArrayList<>();

    @PostConstruct
    void loadDocumentList() {
        File file = docsFile();

        if (!file.isFile() || file.length() == 0) {
            return;
        }

        try {
            List<DocumentMetadata> documents = objectMapper.readValue(
                    file,
                    new TypeReference<List<DocumentMetadata>>() {}
            );

            uploadedDocs.addAll(documents);
        } catch (IOException e) {
            log.error("Could not load document metadata from {}", file, e);
        }
    }

    public synchronized void ingestMaterial(MultipartFile file)
            throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "The uploaded file is empty.");
        }

        String filename = Objects.requireNonNullElse(
                file.getOriginalFilename(), "unnamed");

        String text = extractText(file, filename);

        if (text.isBlank()) {
            throw new IllegalArgumentException(
                    "No readable text found in " + filename);
        }

        String documentId = UUID.randomUUID().toString();

        Document document = new Document(
                text,
                Map.of(
                        "filename", filename,
                        "docId", documentId
                )
        );

        List<Document> chunks =
                new TokenTextSplitter().apply(List.of(document));

        if (chunks.isEmpty()) {
            throw new IllegalArgumentException(
                    "The uploaded document could not be split into chunks.");
        }

        vectorStore.add(chunks);

        List<String> chunkIds = chunks.stream()
                .map(Document::getId)
                .collect(Collectors.toList());

        DocumentMetadata metadata = DocumentMetadata.builder()
                .id(documentId)
                .filename(filename)
                .contentType(Objects.requireNonNullElse(
                        file.getContentType(), "text/plain"))
                .size(file.getSize())
                .uploadDate(LocalDateTime.now())
                .chunkIds(chunkIds)
                .build();

        uploadedDocs.add(metadata);

        saveVectorStore();
        saveDocumentList();

        log.info("Indexed document '{}' with {} chunks",
                filename, chunks.size());
    }

    private String extractText(MultipartFile file, String filename)
            throws IOException {

        if (filename.toLowerCase().endsWith(".pdf")) {
            try (PDDocument pdf = Loader.loadPDF(file.getBytes())) {
                return new PDFTextStripper().getText(pdf);
            }
        }

        return new String(file.getBytes(), StandardCharsets.UTF_8);
    }

    public List<QuizResponse> generateQuiz(
            QuizGenerationRequest request) {

        if (request == null
                || request.getTargetTopic() == null
                || request.getTargetTopic().isBlank()) {
            throw new IllegalArgumentException(
                    "Please provide a topic for the quiz.");
        }

        int requestedCount = request.getNumberOfQuestions();
        int questionCount = Math.max(1, Math.min(requestedCount, 20));

        List<Document> similar = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(request.getTargetTopic())
                        .topK(5)
                        .build()
        );

        if (similar.isEmpty()) {
            throw new IllegalArgumentException(
                    "No relevant study material found. Upload a document first.");
        }

        String context = similar.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        String prompt = """
                You are an educational instructor.
                Generate %d multiple-choice questions about "%s"
                using only the study material below.

                Return only a valid JSON array, without Markdown.
                Each object must contain:
                - questionNumber: integer
                - questionText: string
                - multipleChoiceOptions: array of exactly 4 strings
                - correctAnswer: string matching one option exactly

                Study material:
                %s
                """.formatted(
                questionCount,
                request.getTargetTopic(),
                context
        );

        List<QuizResponse> quiz = chatClientBuilder.build()
                .prompt()
                .user(prompt)
                .call()
                .entity(new ParameterizedTypeReference<List<QuizResponse>>() {});

        return quiz != null ? quiz : new ArrayList<>();
    }

    public ChatResponse answerQuestion(String question) {

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Question must not be empty.");
        }

        List<Document> similar = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(3)
                        .build()
        );

        if (similar.isEmpty()) {
            return ChatResponse.builder()
                    .answer("I couldn't find relevant study material. Please upload a document first.")
                    .sources(List.of())
                    .build();
        }

        String context = similar.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        String prompt = """
                You are a helpful study assistant.
                Answer the student's question using only the provided context.
                If the answer is not supported by the context, say you
                don't have enough information.

                Question:
                %s

                Context:
                %s
                """.formatted(question, context);

        String answer = chatClientBuilder.build()
                .prompt()
                .user(prompt)
                .call()
                .content();

        List<String> sources = similar.stream()
                .map(document -> document.getMetadata().get("filename"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .distinct()
                .collect(Collectors.toList());

        return ChatResponse.builder()
                .answer(answer != null ? answer : "")
                .sources(sources)
                .build();
    }

    public List<DocumentMetadata> listDocuments() {
        return List.copyOf(uploadedDocs);
    }

    public synchronized void deleteDocument(String id) {

        DocumentMetadata metadata = uploadedDocs.stream()
                .filter(document -> Objects.equals(document.getId(), id))
                .findFirst()
                .orElse(null);

        if (metadata == null) {
            return;
        }

        List<String> chunkIds = metadata.getChunkIds();

        if (chunkIds != null && !chunkIds.isEmpty()) {
            vectorStore.delete(chunkIds);
        }

        uploadedDocs.remove(metadata);

        saveVectorStore();
        saveDocumentList();

        log.info("Deleted document '{}'", metadata.getFilename());
    }

    private File dataDirectory() {
        File directory = new File(dataDir);

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException(
                    "Could not create data directory: "
                            + directory.getAbsolutePath());
        }

        return directory;
    }

    private File vaultFile() {
        return new File(dataDirectory(), "study-vault.json");
    }

    private File docsFile() {
        return new File(dataDirectory(), "documents.json");
    }

    private void saveVectorStore() {
        vectorStore.save(vaultFile());
    }

    private void saveDocumentList() {
        try {
            objectMapper.writeValue(docsFile(), uploadedDocs);
        } catch (IOException e) {
            log.error("Failed to save document metadata", e);
            throw new IllegalStateException(
                    "Could not save document metadata.", e);
        }
    }
}
