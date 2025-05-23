package com.semestre5.ProAula.Repository;

import com.semestre5.ProAula.Model.Contaminante;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ContaminanteRepository extends MongoRepository<Contaminante, String> {
    List<Contaminante> findByIdIn(List<String> ids);

}
