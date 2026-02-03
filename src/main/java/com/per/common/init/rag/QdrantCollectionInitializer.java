package com.per.common.init.rag;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import io.qdrant.client.grpc.Collections.Distance;
import io.qdrant.client.grpc.Collections.VectorParams;
import lombok.extern.slf4j.Slf4j;

/**
 * Initializer to ensure Qdrant collection exists before application startup. Creates the vector
 * collection if it doesn't exist with proper dimensions and distance metric. Runs before
 * RagDataInitializer (Order 2 vs Order 3).
 */
@Configuration
@Slf4j
@Order(2) // Run before RagDataInitializer (Order 3)
public class QdrantCollectionInitializer {

    @Value("${spring.ai.vectorstore.qdrant.host}")
    private String host;

    @Value("${spring.ai.vectorstore.qdrant.port}")
    private int grpcPort;

    @Value("${spring.ai.vectorstore.qdrant.collection-name}")
    private String collectionName;

    /** OpenAI text-embedding-3-small model outputs 1536 dimensions */
    private static final int VECTOR_DIMENSION = 1536;

    @PostConstruct
    public void ensureCollectionExists() {
        log.info("Checking Qdrant collection: {}", collectionName);

        try (QdrantClient client =
                new QdrantClient(QdrantGrpcClient.newBuilder(host, grpcPort, false).build())) {

            // Check if collection exists
            boolean exists =
                    client.listCollectionsAsync().get().stream()
                            .anyMatch(c -> c.equals(collectionName));

            if (!exists) {
                log.info(
                        "Creating Qdrant collection: {} (dim={}, distance=Cosine)",
                        collectionName,
                        VECTOR_DIMENSION);

                client.createCollectionAsync(
                                collectionName,
                                VectorParams.newBuilder()
                                        .setSize(VECTOR_DIMENSION)
                                        .setDistance(Distance.Cosine)
                                        .build())
                        .get();

                log.info("Collection {} created successfully", collectionName);
            } else {
                log.debug("Collection {} already exists", collectionName);
            }
        } catch (Exception e) {
            log.error("Failed to initialize Qdrant collection: {}", e.getMessage(), e);
            throw new RuntimeException("Qdrant collection initialization failed", e);
        }
    }
}
