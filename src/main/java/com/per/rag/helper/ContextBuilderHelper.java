package com.per.rag.helper;

import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import com.per.rag.service.MultiCollectionSearchService.MultiSearchResult;

/**
 * Helper class for building context strings from document lists. Formats retrieved documents for
 * inclusion in LLM prompts using strict XML structure to prevent hallucination.
 */
@Component
public class ContextBuilderHelper {

    /**
     * Builds a structured XML context from a list of documents (legacy method for products only).
     */
    public String buildContext(List<Document> documents) {
        if (documents.isEmpty()) {
            return "<Inventory>\n  <empty>No relevant product information available.</empty>\n</Inventory>";
        }
        return buildInventorySection(documents);
    }

    /**
     * Builds a multi-source context with labeled sections for brands, categories, products, and
     * policies.
     */
    public String buildMultiSourceContext(MultiSearchResult result) {
        StringBuilder context = new StringBuilder();
        context.append("<Context>\n");

        // Brand context
        if (!result.brands().isEmpty()) {
            context.append("<BrandContext>\n");
            for (Document doc : result.brands()) {
                context.append("  <brand>\n");
                context.append("    <name>")
                        .append(escapeXml(extractMetadata(doc, "name")))
                        .append("</name>\n");
                context.append("    <details>")
                        .append(escapeXml(doc.getText()))
                        .append("</details>\n");
                context.append("  </brand>\n");
            }
            context.append("</BrandContext>\n");
        }

        // Category context
        if (!result.categories().isEmpty()) {
            context.append("<CategoryContext>\n");
            for (Document doc : result.categories()) {
                context.append("  <category>\n");
                context.append("    <name>")
                        .append(escapeXml(extractMetadata(doc, "name")))
                        .append("</name>\n");
                context.append("    <description>")
                        .append(escapeXml(doc.getText()))
                        .append("</description>\n");
                context.append("  </category>\n");
            }
            context.append("</CategoryContext>\n");
        }

        // Product inventory
        context.append(buildInventorySection(result.products()));

        // Policy/Knowledge context
        if (!result.knowledge().isEmpty()) {
            context.append("<Policy>\n");
            for (Document doc : result.knowledge()) {
                String source = extractMetadata(doc, "source");
                context.append("  <document source=\"")
                        .append(escapeXml(source != null ? source : "unknown"))
                        .append("\">\n");
                context.append(escapeXml(doc.getText()));
                context.append("\n  </document>\n");
            }
            context.append("</Policy>\n");
        }

        context.append("</Context>");
        return context.toString();
    }

    private String buildInventorySection(List<Document> documents) {
        if (documents.isEmpty()) {
            return "<Inventory>\n  <empty>No relevant products found.</empty>\n</Inventory>\n";
        }

        StringBuilder xml = new StringBuilder();
        xml.append("<Inventory>\n");

        for (Document doc : documents) {
            xml.append("  <product_item>\n");

            String productId =
                    doc.getMetadata() != null && doc.getMetadata().containsKey("productId")
                            ? doc.getMetadata().get("productId").toString()
                            : doc.getId();

            xml.append("    <id>").append(escapeXml(productId)).append("</id>\n");

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

            xml.append("    <content>\n");
            xml.append(escapeXml(doc.getText()));
            xml.append("\n    </content>\n");
            xml.append("  </product_item>\n");
        }

        xml.append("</Inventory>\n");
        return xml.toString();
    }

    private String extractMetadata(Document doc, String key) {
        if (doc.getMetadata() == null) return null;
        Object value = doc.getMetadata().get(key);
        return value != null ? value.toString() : null;
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}
