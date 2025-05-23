package com.semestre5.ProAula.Services;

import com.semestre5.ProAula.Model.Contaminante;
import com.semestre5.ProAula.Repository.ContaminanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
@Service
public class ContaminanteService {
    @Autowired
    private ContaminanteRepository contaminanteRepository;

    public List<Contaminante> listarTodosLosContaminantes() {
        return contaminanteRepository.findAll();
    }

    public Contaminante obtenerPorId(String id) {
        return contaminanteRepository.findById(id).orElse(null);
    }

    // Nuevo método para buscar contaminantes por una lista de IDs
    public List<Contaminante> findContaminantesByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return contaminanteRepository.findByIdIn(ids);
    }
}
