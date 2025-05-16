package com.semestre5.ProAula.Services;

import com.semestre5.ProAula.Model.Reportes;
import com.semestre5.ProAula.Repository.ReportesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportesService {
    @Autowired
    private ReportesRepository reportesRepository;

    @Autowired
    private StorageService storageService; // Asumiendo que lo usas para guardar/borrar evidencia

    @Autowired
    private MongoTemplate mongoTemplate; // Para consultas dinámicas

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

    public Reportes actualizarEstadoReporte(String reporteId, Reportes.EstadoReporte nuevoEstado) {
        Reportes reporte = reportesRepository.findById(reporteId)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado con ID: " + reporteId));
        reporte.setEstado(nuevoEstado);
        return reportesRepository.save(reporte);
    }

    public List<Reportes> getReportesFiltrados(
            String barrioId,
            String contaminanteId, // Un solo contaminante para simplificar, buscará si está en la lista del reporte
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Reportes.EstadoReporte estado) {

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        if (StringUtils.hasText(barrioId)) {
            criteriaList.add(Criteria.where("barrioId").is(barrioId));
        }

        // Si se proporciona un contaminanteId, busca reportes que lo incluyan en su lista 'contaminantesIds'
        if (StringUtils.hasText(contaminanteId)) {
            criteriaList.add(Criteria.where("contaminantesIds").is(contaminanteId)); // O .all() si quieres que tenga solo ese, .in() si el filtro es una lista
        }

        if (fechaDesde != null && fechaHasta != null) {
            criteriaList.add(Criteria.where("fechaReporte").gte(fechaDesde).lte(fechaHasta));
        } else if (fechaDesde != null) {
            criteriaList.add(Criteria.where("fechaReporte").gte(fechaDesde));
        } else if (fechaHasta != null) {
            criteriaList.add(Criteria.where("fechaReporte").lte(fechaHasta));
        }

        if (estado != null) {
            criteriaList.add(Criteria.where("estado").is(estado));
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }
        return mongoTemplate.find(query, Reportes.class);
    }

    public Page<Reportes> findAllReportesPaginadosYFiltrados(
            List<String> userIds, // Lista de IDs de usuario si se buscó por nombre/email
            boolean searchTermIsId, // Indica si el término de búsqueda original era un ID de usuario
            String originalUserSearchTerm, // El término de búsqueda original para usuario
            String contaminanteId,
            String barrioId,
            Reportes.EstadoReporte estadoReporte,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            Pageable pageable) {

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        if (userIds != null && !userIds.isEmpty()) {
            criteriaList.add(Criteria.where("userId").in(userIds));
        } else if (searchTermIsId && StringUtils.hasText(originalUserSearchTerm)) {
            // Si se buscó por ID de usuario y no se encontraron usuarios (userIds está vacío),
            // pero el término era un ID, entonces buscar reportes con ese userId directamente.
            if (originalUserSearchTerm.matches("[a-fA-F0-9]{24}")) {
                criteriaList.add(Criteria.where("userId").is(originalUserSearchTerm));
            } else {
                // Si se buscó por ID pero no es un formato válido y no se encontraron usuarios, no devolver nada.
                return new PageImpl<>(List.of(), pageable, 0);
            }
        }
        // Si userIds es null/empty y searchTermIsId es false, no se filtra por usuario.


        if (StringUtils.hasText(contaminanteId)) {
            criteriaList.add(Criteria.where("contaminantesIds").is(contaminanteId));
        }
        if (StringUtils.hasText(barrioId)) {
            criteriaList.add(Criteria.where("barrioId").is(barrioId));
        }
        if (estadoReporte != null) {
            criteriaList.add(Criteria.where("estado").is(estadoReporte));
        }

        Criteria fechaCriteria = null;
        if (fechaDesde != null && fechaHasta != null) {
            fechaCriteria = Criteria.where("fechaReporte").gte(fechaDesde).lte(fechaHasta);
        } else if (fechaDesde != null) {
            fechaCriteria = Criteria.where("fechaReporte").gte(fechaDesde);
        } else if (fechaHasta != null) {
            fechaCriteria = Criteria.where("fechaReporte").lte(fechaHasta);
        }
        if (fechaCriteria != null) {
            criteriaList.add(fechaCriteria);
        }

        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        long count = mongoTemplate.count(query, Reportes.class);
        query.with(pageable); // Aplicar paginación y ordenación a la query de filtros
        List<Reportes> reportes = mongoTemplate.find(query, Reportes.class);

        return new PageImpl<>(reportes, pageable, count);
    }


}
