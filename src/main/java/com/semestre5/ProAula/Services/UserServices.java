package com.semestre5.ProAula.Services;

import com.semestre5.ProAula.Model.User;
import com.semestre5.ProAula.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServices {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> listarTodos(){ return userRepository.findAll(); }

    public User guardar_user(User usuario) {
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        if (usuario.getReportesIds() == null) {
            usuario.setReportesIds(new ArrayList<>());
        }
        return userRepository.save(usuario);
    }
}
