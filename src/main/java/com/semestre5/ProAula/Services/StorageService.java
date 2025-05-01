package com.semestre5.ProAula.Services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class StorageService {

    private final String UPLOAD_DIR = "uploads/"; // Carpeta donde se guardarán los archivos

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
}
