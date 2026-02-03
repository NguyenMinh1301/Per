package com.per.common.init.rag;

import java.util.List;

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
 * Initializer to ensure all Qdrant collections exist before application startup. Creates vector
 * collections for products, brands, and categories if they don't exist. Runs before
 * RagDataInitializer (Order 2 vs Order 3).
 */
@Configuration
@Slf4j
@Order(2)
public class QdrantCollectionInitializer {

    @Value("${spring.ai.vectorstore.qdrant.host}")
    private String host;

    @Value("${spring.ai.vectorstore.qdrant.port}")
    private int grpcPort;

    @Value("${app.rag.qdrant.collections.product:product_vectors}")
    private String productCollection;

    @Value("${app.rag.qdrant.collections.brand:brand_vectors}")
    private String brandCollection;

    @Value("${app.rag.qdrant.collections.category:category_vectors}")
    private String categoryCollection;

    @Value("${app.rag.qdrant.collections.knowledge:knowledge_vectors}")
    private String knowledgeCollection;

    /** OpenAI text-embedding-3-small model outputs 1536 dimensions */
    private static final int VECTOR_DIMENSION = 1536;

    @PostConstruct
    public void ensureCollectionsExist() {
        List<String> collections =
                List.of(
                        productCollection,
                        brandCollection,
                        categoryCollection,
                        knowledgeCollection);

        log.info("Ensuring Qdrant collections exist: {}", collections);

        try (QdrantClient client =
                new QdrantClient(QdrantGrpcClient.newBuilder(host, grpcPort, false).build())) {

            List<String> existingCollections = client.listCollectionsAsync().get();

            for (String collectionName : collections) {
                if (!existingCollections.contains(collectionName)) {
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
            }

            log.info("All Qdrant collections verified");
        } catch (Exception e) {
            log.error("Failed to initialize Qdrant collections: {}", e.getMessage(), e);
            throw new RuntimeException("Qdrant collection initialization failed", e);
        }
    }
}
