package com.semestre5.ProAula.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AuthController { // O agrégalo a UserController

    private final AuthenticationManager authenticationManager;

    // Inyecta AuthenticationManager (necesitas exponerlo como Bean en SecurityConfig)
    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    // DTO para recibir las credenciales del frontend
    public static class LoginRequest {
        private String email;
        private String password;
        // Getters y Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Éxito - Aquí podrías generar un JWT o simplemente devolver OK
            // Si usas sesiones por defecto, Spring Security manejará la sesión.
            System.out.println("Usuario autenticado: " + loginRequest.getEmail()); // Log de éxito
            // Devuelve una respuesta simple de éxito o información del usuario/token
            return ResponseEntity.ok().body("{\"message\": \"Login exitoso!\"}"); // Ejemplo de respuesta JSON

        } catch (Exception e) {
            System.err.println("Error de autenticación para: " + loginRequest.getEmail() + " - " + e.getMessage()); // Log de error
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("{\"error\": \"Credenciales inválidas\"}");
        }
    }
}

