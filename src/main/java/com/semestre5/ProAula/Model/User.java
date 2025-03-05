package com.semestre5.ProAula.Model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;



@Setter
@Getter
@Document(collection = "Usuarios")
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    private String id;

    private String contrasena;

    private String direccion;

    private String email;

    private String nombre;

    private String user_name;


    public enum User_tipo{
        NORMAL, ADMIN, ENTIDAD
    }

    private User_tipo user_tipo;

    private List<String> reportesId;

}
