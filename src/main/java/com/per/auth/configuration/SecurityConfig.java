package com.per.auth.configuration;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.per.auth.security.filter.JwtAuthenticationFilter;
import com.per.auth.security.principal.DatabaseUserDetailsService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final DatabaseUserDetailsService userDetailsService;
    private final ApplicationProperties applicationProperties;

    private static final String[] publicAuthEndpoints = {
        "/per/auth/register",
        "/per/auth/login",
        "/per/auth/refresh",
        "/per/auth/introspect",
        "/per/auth/verify-email",
        "/per/auth/forgot-password",
        "/per/auth/reset-password",
    };

    // GET only - list, detail, search
    private static final String[] publicGetEndpoints = {
        "/per/products/list",
        "/per/products/detail/{id}",
        "/per/products/search",
        "/per/brands/list",
        "/per/brands/detail/{id}",
        "/per/brands/search",
        "/per/categories/list",
        "/per/categories/detail/{id}",
        "/per/categories/search",
        "/per/made-in/list",
        "/per/made-in/detail/{id}",
        "/per/made-in/search",
    };

    private static final String[] publicRAGEndpoints = {"/per/rag/chat"};

    private static final String[] publicOtherEndpoints = {
        "/per/v3/api-docs/**",
        "/per/swagger-ui.html",
        "/per/swagger-ui/**",
        "/per/api-docs",
        "/actuator/prometheus",
        "/per/payments/payos/webhook",
        "/per/payments/payos/return"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(publicAuthEndpoints)
                                        .permitAll()
                                        .requestMatchers(HttpMethod.GET, publicGetEndpoints)
                                        .permitAll()
                                        .requestMatchers(publicRAGEndpoints)
                                        .permitAll()
                                        .requestMatchers(publicOtherEndpoints)
                                        .permitAll()
                                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(false);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(applicationProperties.getCors().getAllowedOrigins());

        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
