package com.semestre5.ProAula.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document (collection = "Contaminantes")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Contaminante {

    private String id_contaminante;

    private String nombre_contaminante;
}
