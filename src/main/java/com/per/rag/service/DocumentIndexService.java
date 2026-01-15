package com.per.rag.service;

/** Service for indexing knowledge base documents (markdown files) into vector store */
public interface DocumentIndexService {

    /** Index all markdown files from knowledge base directory */
    void indexKnowledgeBase();

    /**
     * Index a single markdown file from knowledge base
     *
     * @param filename Name of the markdown file (e.g., "policies.md")
     */
    void indexSingleDocument(String filename);

    /** Clear all knowledge base documents from vector store */
    void clearKnowledgeBase();
}
