package com.semestre5.ProAula.Repository;

import com.semestre5.ProAula.Model.Reportes;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReportesRepository extends MongoRepository<Reportes, String> {
    List<Reportes> findByUserId(String userId);
}
