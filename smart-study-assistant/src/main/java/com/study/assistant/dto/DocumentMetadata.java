package com.study.assistant.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentMetadata {
    private String id;
    private String filename;
    private String contentType;
    private long size;
    private LocalDateTime uploadDate;
    /** IDs of the vector-store chunks created for this document (used for deletion). */
    private List<String> chunkIds;
}
