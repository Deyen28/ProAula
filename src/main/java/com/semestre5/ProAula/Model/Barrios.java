package com.semestre5.ProAula.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Barrios")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Barrios {

    private String id_barrio;

    private String nombre_barrio;
}
