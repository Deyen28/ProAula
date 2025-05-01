package com.semestre5.ProAula.Services;

import com.semestre5.ProAula.Model.User;
import com.semestre5.ProAula.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
@Service
public class UserDatailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Intentando cargar usuario por email: " + email); // DEBUG

        User appUser = userRepository.findByEmail(email);


        if (appUser == null) {
            System.out.println("Usuario NO encontrado: " + email); // DEBUG
            throw new UsernameNotFoundException("Usuario no encontrado con email: " + email);
        }

        // Si llegamos aquí, el usuario fue encontrado
        System.out.println("Usuario ENCONTRADO: " + appUser.getEmail()); // DEBUG

        System.out.println("Password HASH de BD: " + appUser.getContrasena()); // DEBUG (¡No mostrar en producción!)

        return new org.springframework.security.core.userdetails.User(
                appUser.getEmail(), // O el campo que uses como username
                appUser.getContrasena(),       // La CONTRASEÑA CODIFICADA de la BD
                new ArrayList<>()            // Lista de roles/autoridades
        );
    }
}
