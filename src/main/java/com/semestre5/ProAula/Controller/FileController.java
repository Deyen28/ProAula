package com.semestre5.ProAula.Controller;

import com.semestre5.ProAula.Services.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
public class FileController {

    @Autowired
    private StorageService storageService;

    @GetMapping("/uploads/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {

            Resource resource = storageService.loadAsResource(filename);

            String contentType = null;
            try {
                Path filePath = resource.getFile().toPath(); // Obtiene el Path desde el Resource
                contentType = Files.probeContentType(filePath);
            } catch (IOException e) {
                System.err.println("No se pudo determinar el tipo de contenido para: " + filename + " - " + e.getMessage());
            }

            // Si no se pudo determinar, usar un tipo genérico (o puedes decidir no servirlo)
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // Tipo genérico para descarga
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (RuntimeException e) {

            System.err.println("Error al servir archivo: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}

