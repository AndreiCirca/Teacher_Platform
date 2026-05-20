package com.example.TeacherPlatform.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. Rute publice generale
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/schools/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/courses/**").permitAll()

                        // 2. Reguli specifice pentru Certificate
                        .requestMatchers(HttpMethod.GET, "/api/certificates/verify/{code}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/certificates/my").hasAuthority("PROFESOR")
                        .requestMatchers("/api/certificates/**").hasAuthority("ADMIN")

                        // 3. Reguli specifice pentru CourseMaterial
                        .requestMatchers(HttpMethod.GET, "/api/materials/course/{courseId}").hasAnyAuthority("PROFESOR", "FORMATOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/materials/{id}/download").hasAnyAuthority("PROFESOR", "FORMATOR", "ADMIN")
                        .requestMatchers("/api/materials/**").hasAnyAuthority("FORMATOR", "ADMIN")

                        // 4. Reguli pentru celelalte entități existente
                        .requestMatchers("/api/schools/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/users/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/categories/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/courses/**").hasAnyAuthority("ADMIN", "FORMATOR")

                        // 5. Reguli Notificări
                        .requestMatchers("/api/notifications/my/**").authenticated()
                        .requestMatchers("/api/notifications/**").hasAnyAuthority("ADMIN", "FORMATOR", "PROFESOR")

                        // Orice alt request neverificat mai sus cere autentificare generică
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://localhost:5173"
        ));

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        config.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept"
        ));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}