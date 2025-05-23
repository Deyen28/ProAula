package com.semestre5.ProAula.Services;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class StorageService {

    
    private final String UPLOAD_DIR = "/data/uploads/";
    private final Path rootLocation = Paths.get(UPLOAD_DIR);

    public String store(MultipartFile file) {
        try {
            // Crear la carpeta si no existe
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Crear nombre único para el archivo
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

            // Ruta completa donde se guardará
            Path filePath = uploadPath.resolve(filename);

            // Guardar el archivo
            Files.copy(file.getInputStream(), filePath);

            // Retornar el nombre o la ruta que luego puedes guardar en Mongo
            return filename;

        } catch (IOException e) {
            throw new RuntimeException("Error al guardar el archivo: " + e.getMessage(), e);
        }
    }

    // --- MÉTODO NUEVO PARA BORRAR ---
    public void delete(String filename) throws IOException {
        if (filename == null || filename.isBlank()) {
            System.err.println("Intento de borrar archivo con nombre nulo o vacío.");
            return; // O lanzar una excepción si prefieres
        }
        try {
            Path file = rootLocation.resolve(filename).normalize().toAbsolutePath();

            // Medida de seguridad similar a store
            if (!file.getParent().equals(rootLocation.toAbsolutePath())) {
                System.err.println("Intento de borrar archivo fuera del directorio permitido: " + filename);
                return; // No intentar borrar
            }

            Files.deleteIfExists(file); // Intenta borrar el archivo si existe
            System.out.println("Archivo eliminado: " + filename); // Log para confirmar
        } catch (IOException e) {
            // Puedes relanzar una excepción específica o manejarla aquí
            System.err.println("Error al eliminar el archivo: " + filename + " - " + e.getMessage());
            throw new IOException("No se pudo eliminar el archivo: " + filename, e);
        }
    }

    // --- Opcional pero útil: Método para cargar archivos (usado por FileController) ---
    public Resource loadAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize().toAbsolutePath();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("No se pudo leer el archivo: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error al leer el archivo: " + filename, e);
        }
    }
}
