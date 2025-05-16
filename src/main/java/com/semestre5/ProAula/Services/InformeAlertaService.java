package com.semestre5.ProAula.Services;

import com.semestre5.ProAula.Model.InformeAlerta;
import com.semestre5.ProAula.Repository.InformeAlertaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class InformeAlertaService {

    @Autowired
    private InformeAlertaRepository informeAlertaRepository;

    public List<InformeAlerta> findAll() {
        return informeAlertaRepository.findAll();
    }

    public Optional<InformeAlerta> findByIdOptional(String id) {
        return informeAlertaRepository.findById(id);
    }

    public InformeAlerta findById(String id) {
        return informeAlertaRepository.findById(id).orElse(null);
    }

    @Transactional
    public InformeAlerta save(InformeAlerta informeAlerta) {
        if (informeAlerta.getId() == null || informeAlerta.getId().trim().isEmpty()) {
            informeAlerta.setFechaCreacion(LocalDate.now());
        }
        // Asegurar que la lista de reportesIds no sea null
        if (informeAlerta.getReportesIds() == null) {
            informeAlerta.setReportesIds(new ArrayList<>());
        }
        return informeAlertaRepository.save(informeAlerta);
    }

    @Transactional
    public void deleteById(String id) {
        if (!informeAlertaRepository.existsById(id)) {
            throw new RuntimeException("Informe de Alerta no encontrado con ID: " + id);
        }
        informeAlertaRepository.deleteById(id);
    }

}
