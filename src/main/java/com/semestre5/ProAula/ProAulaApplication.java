package com.semestre5.ProAula;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories; 

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.semestre5.ProAula.Repository")
public class ProAulaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProAulaApplication.class, args);
	}

}
