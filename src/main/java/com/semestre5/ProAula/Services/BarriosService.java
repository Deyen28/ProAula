package com.semestre5.ProAula.Services;

import com.semestre5.ProAula.Model.Barrios;
import com.semestre5.ProAula.Repository.BarriosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class BarriosService {

    @Autowired
    private BarriosRepository barriosRepository;


    public List<Barrios> listarTodosLosBarrios() {
        return barriosRepository.findAll();
    }

    public Barrios obtenerPorId(String id) {
        return barriosRepository.findById(id).orElse(null);
    }
}
