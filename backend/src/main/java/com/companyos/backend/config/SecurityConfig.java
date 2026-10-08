package com.companyos.backend.config;

import com.companyos.backend.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.config.Customizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;

/**
 * SecurityConfig - defines which routes are public and which require a valid JWT.
 *
 * ACCESS DENIED / AUTH ENTRY POINT
 * ---------------------------------
 * Spring Security's ExceptionTranslationFilter intercepts AccessDeniedException
 * before @RestControllerAdvice can see it. We wire JSON handlers here so the
 * response format is consistent with GlobalExceptionHandler.
 *
 * authenticationEntryPoint -> 401 when no/invalid token reaches a protected route.
 * accessDeniedHandler      -> 403 when @PreAuthorize denies access (wrong role).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .cors(Customizer.withDefaults())           // apply CorsConfig.corsConfigurationSource
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/api/auth/verify").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/verify").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/api/auth/resend-verification").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/resend-verification").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/api/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                // Google OAuth endpoints — token verification is server-side, so these
                // endpoints are safe without an existing JWT.
                .requestMatchers(HttpMethod.OPTIONS, "/api/auth/google").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/google").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/api/auth/google/callback").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/google/callback").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/api/auth/google/verify-otp").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/google/verify-otp").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/google/resend-otp").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/google/exchange").permitAll()
                .anyRequest().authenticated()
            )

            .exceptionHandling(ex -> ex

                // 401 - no token or invalid token on a protected route
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(jsonError(401, "Authentication required. Please log in."));
                })

                // 403 - authenticated but @PreAuthorize denied (insufficient role)
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.getWriter().write(jsonError(403, "Access denied: insufficient permissions"));
                })
            )

            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** Build a JSON error envelope without depending on an ObjectMapper bean. */
    private static String jsonError(int status, String message) {
        return "{\"timestamp\":\"" + LocalDateTime.now() + "\","
             + "\"status\":"     + status + ","
             + "\"error\":\""    + message + "\"}";
    }
}
