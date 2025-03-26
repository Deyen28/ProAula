package com.semestre5.ProAula.Model;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document(collection = "Reportes")

public class Reportes {
    @Id
    private String id;
    private String direccion;
    private String descripcion;
    private LocalDate fechaReporte;
    private String evidencia; // URL o base64

    // Referencia al usuario
    @DBRef
    private User usuario;

    private BarrioInfo barrio;

    private ContaminanteInfo contaminante;

    public enum EstadoReporte {
        PENDIENTE, EN_PROCESO, RESUELTO
    }
    private EstadoReporte estado;

    public static class BarrioInfo {
        private String id;
        private String nombre;

        public BarrioInfo() {
        }

        public BarrioInfo(String id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    public static class ContaminanteInfo {
        private String id;
        private String nombre;

        public ContaminanteInfo() {
        }

        public ContaminanteInfo(String id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }
    }

    public Reportes() {
    }

    public Reportes(String id, String direccion, String descripcion, LocalDate fechaReporte, String evidencia, User usuario, BarrioInfo barrio, ContaminanteInfo contaminante, EstadoReporte estado) {
        this.id = id;
        this.direccion = direccion;
        this.descripcion = descripcion;
        this.fechaReporte = fechaReporte;
        this.evidencia = evidencia;
        this.usuario = usuario;
        this.barrio = barrio;
        this.contaminante = contaminante;
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

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    public BarrioInfo getBarrio() {
        return barrio;
    }

    public void setBarrio(BarrioInfo barrio) {
        this.barrio = barrio;
    }

    public ContaminanteInfo getContaminante() {
        return contaminante;
    }

    public void setContaminante(ContaminanteInfo contaminante) {
        this.contaminante = contaminante;
    }

    public EstadoReporte getEstado() {
        return estado;
    }

    public void setEstado(EstadoReporte estado) {
        this.estado = estado;
    }
}