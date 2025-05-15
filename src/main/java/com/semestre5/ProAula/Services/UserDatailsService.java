package com.semestre5.ProAula.Services;

import com.semestre5.ProAula.Model.User;
import com.semestre5.ProAula.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserDatailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("Intentando cargar usuario por email: " + email); // DEBUG

        User appUser = userRepository.findByEmail(email);
        if (appUser == null) {
            throw new UsernameNotFoundException("Usuario no encontrado con email: " + email);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (appUser.getUserTipo() != null) {
            authorities.add(new SimpleGrantedAuthority(appUser.getUserTipo().name()));
            System.out.println("Autoridad asignada: " + appUser.getUserTipo().name());
        } else {
            authorities.add(new SimpleGrantedAuthority("NORMAL"));
            System.out.println("Advertencia: userTipo es null para " + email + ", asignada autoridad NORMAL.");
        }

        return new org.springframework.security.core.userdetails.User(
                appUser.getEmail(),
                appUser.getContrasena(),
                authorities
        );
    }
}
