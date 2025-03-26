package com.semestre5.ProAula.Model;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDate;
import java.util.List;

@Document(collection = "Informes_Alerta")
public class InformeAlerta {

    @Id
    private String id;
    private String descripcion;

    public enum EstadoInforme {
        SIN_RESOLVER, EN_PROCESO, RESUELTO
    }

    private EstadoInforme estado;

    public enum ValoracionRiesgo {
        BAJA, MEDIA, ALTA, CRITICA
    }

    private ValoracionRiesgo valoracion;
    private LocalDate fechaCreacion;


    private Reportes.BarrioInfo barrio;


    private List<Reportes.ContaminanteInfo> contaminantes;


}

