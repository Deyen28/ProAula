package com.semestre5.ProAula.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document
@Data
@AllArgsConstructor
public class User {

    @Id
    private String id;

    private String contrasena;

    private String direccion;

    private String email;

    private String nombre;

    private String user_name;

    private User_tipo user_tipo;

    public enum User_tipo{
        NORMAL, ADMIN, ENTIDAD
    }

    private List<String> reportesId;

}
