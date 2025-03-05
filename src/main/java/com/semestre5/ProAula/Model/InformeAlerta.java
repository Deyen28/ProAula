package com.semestre5.ProAula.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "Informes_Alerta")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InformeAlerta {

    private String id_informeAlerta;
    private String descripcion;
    private EstadoInforme estadoInforme;
    private ValoracionRiesgo valoracionRiesgo;
    private LocalDate fecha_creacion;

    private String barrio_id;
    private String contaminantes_id;


    public enum EstadoInforme{
        SIN_RESOLVER, EN_PROCESO, RESUELTO
    }

    public enum ValoracionRiesgo {
        BAJA, MEDIA, ALTA, CRITICA
    }

}
