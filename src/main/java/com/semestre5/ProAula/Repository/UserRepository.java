package com.semestre5.ProAula.Repository;

import com.semestre5.ProAula.Model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRepository extends MongoRepository <User, String> {

    User findByEmail(String email);
    List<User> findByIdIn(List<String> ids);
}
