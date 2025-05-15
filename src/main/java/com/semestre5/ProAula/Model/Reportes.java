package com.semestre5.ProAula.Model;



import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "Reportes")

public class Reportes {
    @Id
    private String id;
    private String direccion;
    private String descripcion;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDate fechaReporte;
    private String evidencia; // URL o base64

    // Referencia al usuario
    @Field("user_id")
    private String userId;

    @Field("barrio_id")
    private String barrioId;

    @Field("contaminantes_ids")
    private List<String> contaminantesIds;


    public enum EstadoReporte {
        PENDIENTE, EN_PROCESO, RESUELTO
    }

    private EstadoReporte estado;


    public Reportes() {
    }

    public Reportes(String id, String direccion, String descripcion, LocalDate fechaReporte, String evidencia, String userId, String barrioId, List<String> contaminantesIds, EstadoReporte estado) {
        this.id = id;
        this.direccion = direccion;
        this.descripcion = descripcion;
        this.fechaReporte = fechaReporte;
        this.evidencia = evidencia;
        this.userId = userId;
        this.barrioId = barrioId;
        this.contaminantesIds = contaminantesIds;
        this.estado = estado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDate getFechaReporte() {
        return fechaReporte;
    }

    public void setFechaReporte(LocalDate fechaReporte) {
        this.fechaReporte = fechaReporte;
    }

    public String getEvidencia() {
        return evidencia;
    }

    public void setEvidencia(String evidencia) {
        this.evidencia = evidencia;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public EstadoReporte getEstado() {
        return estado;
    }

    public void setEstado(EstadoReporte estado) {
        this.estado = estado;
    }
}
