package com.semestre5.ProAula.Model;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "Usuarios")

public class User {

    @Id
    private String id;
    private String contrasena;
    private String direccion;

    @Indexed(unique = true)
    private String email;
    private String nombre;
    private String user_name;


    public enum UserTipo{
        NORMAL, ADMIN, ENTIDAD
    }

    private UserTipo userTipo;


    public User() {
    }

    public User(String id, String contrasena, String direccion, String email, String nombre, String user_name, UserTipo userTipo) {
        this.id = id;
        this.contrasena = contrasena;
        this.direccion = direccion;
        this.email = email;
        this.nombre = nombre;
        this.user_name = user_name;
        this.userTipo = userTipo;

    }

    public UserTipo getUserTipo() {
        return userTipo;
    }

    public void setUserTipo(UserTipo userTipo) {
        this.userTipo = userTipo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }


}
