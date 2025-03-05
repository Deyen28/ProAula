package com.semestre5.ProAula.Repository;

import com.semestre5.ProAula.Model.InformeAlerta;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface InformeAlertaRepository extends MongoRepository<InformeAlerta,String> {
}
