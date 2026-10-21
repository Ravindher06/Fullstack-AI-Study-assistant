
package com.study.assistant.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class VectorStoreConfig {

    @Bean
    public SimpleVectorStore vectorStore(
            EmbeddingModel embeddingModel,
            @Value("${study.data-dir:./data}") String dataDir) {

        File directory = new File(dataDir);

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException(
                    "Could not create data directory: " + directory.getAbsolutePath());
        }

        File vault = new File(directory, "study-vault.json");

        SimpleVectorStore store =
                SimpleVectorStore.builder(embeddingModel).build();

        if (vault.isFile() && vault.length() > 0) {
            store.load(vault);
        }

        return store;
    }
}
