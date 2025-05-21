package com.semestre5.ProAula.SConfig;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


import java.util.Arrays;
import java.util.List;
@Configuration
@EnableWebSecurity
@EnableRedisHttpSession
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        // --- Permisos Públicos ---
                        .requestMatchers("/", "/index.html", "/favicon.ico", "/manifest.json", "/static/**", "/imagenes/**","/uploads/**").permitAll()
                        .requestMatchers("/registerView", "/registrar", "/LoginView").permitAll()
                        .requestMatchers("/reportes-publicos").permitAll()
                        // URLs necesarias para el proceso de login/logout (importante permitirlas):
                        .requestMatchers("/perform_login").permitAll()
                        .requestMatchers("/LoginView?error=true").permitAll()
                        .requestMatchers("/LoginView?logout=true").permitAll()
                        // --- Permisos Protegidos ---
                        .requestMatchers("/user/dashboard", "/user/**").authenticated()
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")
                        .anyRequest().authenticated()
                )
                // --- Configuración del Formulario de Login ---
                .formLogin(form -> form
                                .loginPage("/LoginView")
                                .loginProcessingUrl("/perform_login")
                                .successHandler((request, response, authentication) -> {
                                    String targetUrl = "/user/dashboard";

                                    boolean isAdmin = authentication.getAuthorities().stream()
                                            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ADMIN"));
                                    boolean isEntidad = authentication.getAuthorities().stream()
                                            .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ENTIDAD"));
                                    if (isAdmin) {
                                        targetUrl = "/admin/dashboard";
                                    } else if (isEntidad) {
                                        targetUrl = "/entidad/dashboard";
                                        // O si no tienes dashboard de entidad, redirige a user: targetUrl = "/user/dashboard";
                                    }
                                    response.sendRedirect(request.getContextPath() + targetUrl);
                                })
                                .failureUrl("/LoginView?error=true")  // A dónde ir si el login falla
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/LoginView?logout=true")
                        .permitAll()
                );
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/css/**",
                "/js/**",
                "/imagenes/**",
                "/uploads/**", // Para la evidencia
                "/favicon.ico"
        );
    }


    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Origen de tu frontend React (ajusta el puerto si es diferente)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://127.0.0.1:3000","https://proaula-production-43a0.up.railway.app")); // O el puerto donde corra tu React
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type")); // Headers comunes para API
        configuration.setAllowCredentials(true); // Importante si manejas cookies/sesiones
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplicar CORS a todas las rutas
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
