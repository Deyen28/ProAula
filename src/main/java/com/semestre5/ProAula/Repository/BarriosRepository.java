package com.semestre5.ProAula.Repository;

import com.semestre5.ProAula.Model.Barrios;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BarriosRepository extends MongoRepository<Barrios, String> {

    List<Barrios> findByIdIn(List<String> ids);
}
