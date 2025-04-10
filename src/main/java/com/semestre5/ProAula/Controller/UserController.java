package com.semestre5.ProAula.Controller;

import com.semestre5.ProAula.Model.User;


import com.semestre5.ProAula.Services.UserServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
public class UserController {

    @Autowired
    private UserServices userServices;

    List<User> usuarios = new ArrayList<User>();

    @PostMapping("/user/registrar")
    public ResponseEntity<User> registar_user(@RequestBody User usuario){
        usuario.setUserTipo(User.UserTipo.NORMAL);
        User usuarioGuardado = userServices.guardar_user(usuario);
        return ResponseEntity.ok(usuarioGuardado);
    }

    @GetMapping("/user/mostrarUsers")
    public List<User> obtenerALL_user(){
        return  userServices.listarTodos();
    }
}
