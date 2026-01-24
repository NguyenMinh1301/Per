package com.per.rag.helper;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

/**
 * Helper class for building context strings from document lists. Formats retrieved documents for
 * inclusion in LLM prompts using strict XML structure to prevent hallucination.
 */
@Component
public class ContextBuilderHelper {

    /**
     * Builds a structured XML context from a list of documents. Each document is wrapped in
     * &lt;product_item&gt; tags with explicit ID extraction from metadata to prevent LLM from
     * hallucinating placeholder product data.
     *
     * @param documents List of documents retrieved from vector store
     * @return Formatted XML context string for LLM prompt
     */
    public String buildContext(List<Document> documents) {
        if (documents.isEmpty()) {
            return "<inventory>\n  <empty>No relevant product information available.</empty>\n</inventory>";
        }

        StringBuilder xml = new StringBuilder();
        xml.append("<inventory>\n");

        for (Document doc : documents) {
            xml.append("  <product_item>\n");

            // CRITICAL: Extract ID from metadata first, fallback to doc.getId()
            String productId =
                    doc.getMetadata() != null && doc.getMetadata().containsKey("productId")
                            ? doc.getMetadata().get("productId").toString()
                            : doc.getId();

            xml.append("    <id>").append(escapeXml(productId)).append("</id>\n");

            // Extract product name and brand from metadata if available
            if (doc.getMetadata() != null) {
                String productName = extractMetadata(doc, "productName");
                String brandName = extractMetadata(doc, "brandName");

                if (productName != null || brandName != null) {
                    xml.append("    <metadata>\n");
                    if (productName != null) {
                        xml.append("      <productName>")
                                .append(escapeXml(productName))
                                .append("</productName>\n");
                    }
                    if (brandName != null) {
                        xml.append("      <brandName>")
                                .append(escapeXml(brandName))
                                .append("</brandName>\n");
                    }
                    xml.append("    </metadata>\n");
                }
            }

            // Wrap the full document content
            xml.append("    <content>\n");
            xml.append(escapeXml(doc.getText()));
            xml.append("\n    </content>\n");

            xml.append("  </product_item>\n");
        }

        xml.append("</inventory>");
        return xml.toString();
    }

    /**
     * Extracts a metadata value as String, handling null cases.
     *
     * @param doc The document
     * @param key The metadata key
     * @return The metadata value as String, or null if not present
     */
    private String extractMetadata(Document doc, String key) {
        Object value = doc.getMetadata().get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Escapes XML special characters to prevent parsing errors.
     *
     * @param text The text to escape
     * @return XML-safe text
     */
    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
