package com.per.rag.dto.response;

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
    private int products;
    private int brands;
    private int categories;
    private int knowledge;
    private boolean isIndexed;
}
