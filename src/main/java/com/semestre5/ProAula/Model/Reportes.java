package com.semestre5.ProAula.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "Reportes")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Reportes {

    @Id
    private String idReporte;

    private String direccion;

    private String descripcion;

    private LocalDate fecha_reporte;

    private String user_Id;


}
