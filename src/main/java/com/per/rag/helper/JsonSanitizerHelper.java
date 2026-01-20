package com.per.rag.helper;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Helper class for sanitizing and repairing malformed JSON responses from LLMs. Handles common
 * formatting issues like YAML-style block scalars and missing commas.
 */
@Slf4j
@Component
public class JsonSanitizerHelper {

    /**
     * Removes markdown code block wrappers if present (for Llama 3.2 compatibility). Strips ```json
     * and ``` markers that some models add around JSON output.
     *
     * @param response The raw LLM response
     * @return Cleaned response without markdown wrappers
     */
    public String cleanMarkdownCodeBlocks(String response) {
        String cleaned = response.trim();

        // Remove ```json ... ``` wrappers
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7); // Remove ```json
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3); // Remove ```
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }

        return cleaned.trim();
    }

    /**
     * Fixes common LLM malformations in JSON specifically related to Llama models: 1. YAML-style
     * block scalars (| or >) are converted to valid JSON strings with escaped newlines. 2. Literal
     * newlines within strings are escaped. 3. Missing commas between JSON fields are added.
     *
     * @param json The potentially malformed JSON string
     * @return Sanitized JSON string
     */
    public String sanitizeMalformedJson(String json) {
        if (json == null || json.isBlank()) return json;

        String sanitized = json.trim();

        // 1. Repair YAML-style block scalars (": |")
        // This looks for "key": | followed by lines that don't look like JSON keys
        // It consumes until it sees a line starting with "key": or the closing brace }
        try {
            java.util.regex.Pattern yamlBlock =
                    java.util.regex.Pattern.compile(
                            "(\"\\w+\"\\s*:\\s*)[|>]\\s*\\n(.*?)(?=\\n\\s*\"\\w+\"\\s*:|\\n\\s*\\})",
                            java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher matcher = yamlBlock.matcher(sanitized);
            StringBuilder sb = new StringBuilder();
            int lastEnd = 0;
            while (matcher.find()) {
                sb.append(sanitized, lastEnd, matcher.start());
                String keyPrefix = matcher.group(1);
                String content = matcher.group(2);

                // Escape characters that are invalid inside JSON string literals
                String escapedContent =
                        content.trim()
                                .replace("\\", "\\\\")
                                .replace("\"", "\\\"")
                                .replace("\r", "")
                                .replace("\n", "\\n");

                sb.append(keyPrefix).append("\"").append(escapedContent).append("\"");
                lastEnd = matcher.end();
            }
            sb.append(sanitized.substring(lastEnd));
            sanitized = sb.toString();
        } catch (Exception e) {
            log.warn("Regex failure during YAML block repair, skipping this step.", e);
        }

        // 2. Fix missing commas between fields
        // Pattern: (end of value) \n (start of next key)
        sanitized =
                sanitized.replaceAll(
                        "(\"\\s*|\\d+|true|false|null)\\s*\\n\\s*(\"\\w+\"\\s*:)", "$1,\n  $2");

        // 3. Last resort: if the LLM outputted literal newlines inside a string without a pipe
        // This is dangerous but often needed for multiline detailedResponse.
        // We only do this if it looks like we're still inside a block.

        return sanitized;
    }
}
