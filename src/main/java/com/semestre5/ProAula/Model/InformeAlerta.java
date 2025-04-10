package com.semestre5.ProAula.Model;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;

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

    public enum ValoracionRiesgo {
        BAJA, MEDIA, ALTA, CRITICA
    }

    private EstadoInforme estado;
    private ValoracionRiesgo valoracion;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDate fechaCreacion;

    @Field("barrio_id")
    private String barrioId;

    @Field("contaminantes_ids")
    private List<String> contaminantesIds;

    public InformeAlerta() {
    }

    public InformeAlerta(String id, String descripcion, EstadoInforme estado, ValoracionRiesgo valoracion, LocalDate fechaCreacion, String barrioId, List<String> contaminantesIds) {
        this.id = id;
        this.descripcion = descripcion;
        this.estado = estado;
        this.valoracion = valoracion;
        this.fechaCreacion = fechaCreacion;
        this.barrioId = barrioId;
        this.contaminantesIds = contaminantesIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public EstadoInforme getEstado() {
        return estado;
    }

    public void setEstado(EstadoInforme estado) {
        this.estado = estado;
    }

    public ValoracionRiesgo getValoracion() {
        return valoracion;
    }

    public void setValoracion(ValoracionRiesgo valoracion) {
        this.valoracion = valoracion;
    }

    public LocalDate getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDate fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getBarrioId() {
        return barrioId;
    }

    public void setBarrioId(String barrioId) {
        this.barrioId = barrioId;
    }

    public List<String> getContaminantesIds() {
        return contaminantesIds;
    }

    public void setContaminantesIds(List<String> contaminantesIds) {
        this.contaminantesIds = contaminantesIds;
    }
}

