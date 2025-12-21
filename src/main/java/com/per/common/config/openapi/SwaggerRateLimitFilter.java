package com.per.common.config.openapi;

import java.io.IOException;
import java.time.Instant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.per.common.response.ApiResponse;
import com.per.common.exception.ApiErrorCode;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.RequiredArgsConstructor;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@RequiredArgsConstructor
public class SwaggerRateLimitFilter extends OncePerRequestFilter {

    private final RateLimiterRegistry rateLimiterRegistry;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        // Check if request is for Swagger UI or API Docs
        if (path.startsWith("/per/api-docs")
                || path.startsWith("/per/v3/api-docs")
                || path.startsWith("/per/swagger-ui")) {

            // Exclude static assets
            // if (path.endsWith(".css")
            //        || path.endsWith(".js")
            //        || path.endsWith(".png")
            //        || path.endsWith(".ico")) {
            //    filterChain.doFilter(request, response);
            //    return;
            // }

            RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter("swagger");

            if (!rateLimiter.acquirePermission()) {
                handleRateLimitExceeded(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void handleRateLimitExceeded(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Void> apiResponse =
                ApiResponse.failure(
                        ApiErrorCode.TOO_MANY_REQUESTS,
                        ApiErrorCode.TOO_MANY_REQUESTS.getDefaultMessage(),
                        null);
        // Ensure timestamp is set if not auto-set (Builder default usually handles it)
        if (apiResponse.getTimestamp() == null) {
            apiResponse.setTimestamp(Instant.now());
        }

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
