package com.per.rag.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeStatusResponse {
    private int totalDocuments;
    private int knowledgeDocuments;
    private int productDocuments;
    private List<String> knowledgeSources;
    private boolean isIndexed;
}
