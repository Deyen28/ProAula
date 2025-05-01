package com.semestre5.ProAula.Services;

import com.semestre5.ProAula.Model.User;
import com.semestre5.ProAula.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public User guardar(User usuario) {
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        return userRepository.save(usuario);
    }

    public User obtenerPorEmail(String email) {
        return userRepository.findByEmail(email);
    }


    public User obtenerPorId(String id) {
        return userRepository.findById(id).orElse(null);
    }


    public List<User> listarTodos() {
        return userRepository.findAll();
    }


    public User actualizarUsuario(User updatedUser) {
        if (updatedUser.getId() != null && userRepository.existsById(updatedUser.getId())) {
            return userRepository.save(updatedUser);
        }
        throw new RuntimeException("Usuario no encontrado para actualizar.");
    }
}
