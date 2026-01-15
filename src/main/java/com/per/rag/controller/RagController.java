package com.per.rag.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.per.common.ApiConstants;
import com.per.common.base.BaseController;
import com.per.common.exception.ApiErrorCode;
import com.per.common.response.ApiResponse;
import com.per.common.response.ApiSuccessCode;
import com.per.rag.dto.response.IndexStatusResponse;
import com.per.rag.service.ChatService;
import com.per.rag.service.VectorStoreService;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping(ApiConstants.Rag.ROOT)
@RequiredArgsConstructor
@Tag(name = "RAG", description = "Shopping Assistant AI APIs")
public class RagController extends BaseController {

    private final ChatService chatService;
    private final VectorStoreService vectorStoreService;
    private final com.per.rag.service.DocumentIndexService documentIndexService;

    @PostMapping(ApiConstants.Rag.CHAT)
    @RateLimiter(name = "mediumTraffic", fallbackMethod = "rateLimit")
    public ResponseEntity<ApiResponse<com.per.rag.dto.response.ChatResponse>> chat(
            @Valid @RequestBody com.per.rag.dto.request.ChatRequest request) {
        com.per.rag.dto.response.ChatResponse response = chatService.chat(request.getQuestion());
        return ResponseEntity.ok(ApiResponse.success(ApiSuccessCode.RAG_CHAT_SUCCESS, response));
    }

    @GetMapping(value = ApiConstants.Rag.CHAT_STREAM, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @RateLimiter(name = "mediumTraffic", fallbackMethod = "rateLimit")
    public Flux<ServerSentEvent<String>> chatStream(@RequestParam String question) {
        return chatService
                .chatStream(question)
                .map(chunk -> ServerSentEvent.<String>builder().data(chunk).build());
    }

    @PostMapping(ApiConstants.Rag.INDEX)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<IndexStatusResponse>> reindexProducts() {
        try {
            log.info("Starting product indexing for AI assistant");
            vectorStoreService.indexAllProducts();

            IndexStatusResponse response =
                    IndexStatusResponse.builder()
                            .status("Indexing completed successfully")
                            .documentsIndexed(0)
                            .build();

            return ResponseEntity.status(HttpStatus.OK)
                    .body(ApiResponse.success(ApiSuccessCode.RAG_INDEX_SUCCESS, response));
        } catch (Exception e) {
            log.error("Failed to reindex products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(ApiErrorCode.RAG_INDEXING_FAILED));
        }
    }

    @PostMapping(ApiConstants.Rag.INDEX_KNOWLEDGE)
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "mediumTraffic", fallbackMethod = "rateLimit")
    @Operation(
            summary = "Index knowledge base",
            description = "Index all markdown files from knowledge base directory (Admin only)")
    public ResponseEntity<ApiResponse<IndexStatusResponse>> indexKnowledgeBase() {
        try {
            log.info("Starting knowledge base indexing");
            documentIndexService.indexKnowledgeBase();

            IndexStatusResponse response =
                    IndexStatusResponse.builder()
                            .status("Knowledge base indexed successfully")
                            .documentsIndexed(0)
                            .build();

            return ResponseEntity.ok(
                    ApiResponse.success(ApiSuccessCode.RAG_KNOWLEDGE_INDEX_SUCCESS, response));
        } catch (Exception e) {
            log.error("Failed to index knowledge base", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(ApiErrorCode.RAG_KNOWLEDGE_INDEX_FAILED));
        }
    }

    @DeleteMapping(ApiConstants.Rag.DELETE_KNOWLEDGE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Clear knowledge base",
            description = "Remove all knowledge base documents from vector store (Admin only)")
    public ResponseEntity<ApiResponse<Void>> clearKnowledgeBase() {
        try {
            log.info("Clearing knowledge base");
            documentIndexService.clearKnowledgeBase();

            return ResponseEntity.ok(
                    ApiResponse.success(ApiSuccessCode.RAG_KNOWLEDGE_DELETE_SUCCESS));
        } catch (Exception e) {
            log.error("Failed to clear knowledge base", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(ApiErrorCode.RAG_KNOWLEDGE_INDEX_FAILED));
        }
    }

    @GetMapping(ApiConstants.Rag.KNOWLEDGE_STATUS)
    @Operation(
            summary = "Get knowledge base status",
            description = "Check if knowledge base is indexed and view document counts")
    public ResponseEntity<ApiResponse<com.per.rag.dto.response.KnowledgeStatusResponse>>
            getKnowledgeStatus() {
        try {
            java.util.Map<String, Object> statusMap = vectorStoreService.getKnowledgeStatus();

            com.per.rag.dto.response.KnowledgeStatusResponse response =
                    com.per.rag.dto.response.KnowledgeStatusResponse.builder()
                            .totalDocuments((Integer) statusMap.get("totalDocuments"))
                            .knowledgeDocuments((Integer) statusMap.get("knowledgeDocuments"))
                            .productDocuments((Integer) statusMap.get("productDocuments"))
                            .knowledgeSources(
                                    (java.util.List<String>) statusMap.get("knowledgeSources"))
                            .isIndexed((Boolean) statusMap.get("isIndexed"))
                            .build();

            return ResponseEntity.ok(
                    ApiResponse.success(ApiSuccessCode.RAG_CHAT_SUCCESS, response));
        } catch (Exception e) {
            log.error("Failed to get knowledge status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.failure(ApiErrorCode.RAG_SEARCH_FAILED));
        }
    }
}
