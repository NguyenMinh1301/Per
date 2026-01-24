package com.per.rag.helper;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Helper class for sanitizing and repairing malformed JSON responses from LLMs. Handles common
 * formatting issues like YAML-style block scalars, missing commas, and literal newline sequences.
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
     * Fixes common LLM malformations in JSON: 1. YAML-style block scalars (| or >) are converted to
     * valid JSON strings with escaped newlines. 2. Literal backslash-n sequences (\n appearing as
     * two characters in the response) are converted to actual escaped newlines. 3. Unescaped actual
     * newlines within JSON string values are properly escaped. 4. Missing commas between JSON
     * fields are added.
     *
     * @param json The potentially malformed JSON string
     * @return Sanitized JSON string
     */
    public String sanitizeMalformedJson(String json) {
        if (json == null || json.isBlank()) return json;

        String sanitized = json.trim();

        // 1. Fix literal backslash-n appearing as two characters "\" followed by "n"
        // Some LLMs output this instead of actual newline escape sequence
        // This is a critical fix for the error shown in logs
        sanitized = sanitized.replace("\\n", "\n");

        // 2. Repair YAML-style block scalars (": |")
        // This looks for "key": | followed by lines that don't look like JSON keys
        // It consumes until it sees a line starting with "key": or the closing brace }
        try {
            java.util.regex.Pattern yamlBlock =
                    java.util.regex.Pattern.compile(
                            "(\"\\w+\"\\s*:\\s*)[|>]\\s*\n(.*?)(?=\n\\s*\"\\w+\"\\s*:|\n\\s*\\})",
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

        // 3. Escape unescaped newlines within JSON string values
        // This is critical for the second error in logs where actual newlines appear in strings
        sanitized = escapeUnescapedNewlines(sanitized);

        // 4. Fix missing commas between fields
        // Pattern: (end of value) \n (start of next key)
        sanitized =
                sanitized.replaceAll(
                        "(\"\\s*|\\d+|true|false|null)\\s*\n\\s*(\"\\w+\"\\s*:)", "$1,\n  $2");

        return sanitized;
    }

    /**
     * Escapes unescaped newlines that appear within JSON string values. This handles the case where
     * LLMs output actual newline characters inside strings instead of \\n escape sequences.
     *
     * @param json The JSON string potentially containing unescaped newlines
     * @return JSON with newlines properly escaped
     */
    private String escapeUnescapedNewlines(String json) {
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escaped) {
                // Previous character was backslash, don't process this one specially
                result.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\') {
                // Mark that next character is escaped
                result.append(c);
                escaped = true;
                continue;
            }

            if (c == '"') {
                // Toggle string state
                inString = !inString;
                result.append(c);
                continue;
            }

            if (c == '\n' && inString) {
                // Found an unescaped newline inside a string - escape it
                result.append("\\n");
            } else if (c == '\r' && inString) {
                // Remove carriage returns inside strings
                continue;
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}
