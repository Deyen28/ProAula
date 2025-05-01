package com.semestre5.ProAula.Repository;

import com.semestre5.ProAula.Model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository <User, String> {

    User findByEmail(String email);
}
