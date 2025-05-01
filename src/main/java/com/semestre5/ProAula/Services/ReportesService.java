package com.semestre5.ProAula.Services;

import com.semestre5.ProAula.Model.Reportes;
import com.semestre5.ProAula.Repository.ReportesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ReportesService {
    @Autowired
    private ReportesRepository reportesRepository;

    @Autowired
    private StorageService storageService; // Para guardar archivos si tienes evidencia en imágenes.

    public Reportes guardar(Reportes reporte, MultipartFile evidencia) {
        if (evidencia != null && !evidencia.isEmpty()) {
            String evidenciaUrl = storageService.store(evidencia);
            reporte.setEvidencia(evidenciaUrl);
        }
        return reportesRepository.save(reporte);
    }

    public Reportes obtenerPorId(String id) {
        return reportesRepository.findById(id).orElse(null);
    }

    public List<Reportes> obtenerReportesPorUsuario(String userId) {
        return reportesRepository.findByUserId(userId);
    }

    public Reportes actualizar(Reportes reporte) {
        return reportesRepository.save(reporte);
    }

    public void eliminar(String id) {
        reportesRepository.deleteById(id);
    }

    public List<Reportes> listarTodos() {
        return reportesRepository.findAll();
    }
}
