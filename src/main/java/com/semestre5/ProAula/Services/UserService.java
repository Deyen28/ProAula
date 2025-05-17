package com.semestre5.ProAula.Services;

import com.semestre5.ProAula.Model.User;
import com.semestre5.ProAula.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MongoTemplate mongoTemplate;


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

    @Transactional
    public void eliminarUsuario(String userId) {

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("Usuario no encontrado para eliminar con ID: " + userId);
        }
        userRepository.deleteById(userId);
        System.out.println("Usuario eliminado con ID: " + userId); // Log
    }

    public Page<User> findUsuariosPaginadosYFiltrados(String searchTerm, String searchBy, Pageable pageable) {
        Query query = new Query(); // Query para filtros y luego para datos paginados
        List<Criteria> orCriteriaList = new ArrayList<>();

        if (StringUtils.hasText(searchTerm)) {
            if ("id".equalsIgnoreCase(searchBy)) {
                if (searchTerm.matches("[a-fA-F0-9]{24}")) { // Validar formato ObjectId
                    query.addCriteria(Criteria.where("_id").is(searchTerm));
                } else {
                    return new PageImpl<>(List.of(), pageable, 0); // ID no válido, no hay resultados
                }
            } else if ("email".equalsIgnoreCase(searchBy)) {
                query.addCriteria(Criteria.where("email").regex(searchTerm, "i"));
            } else if (!StringUtils.hasText(searchBy)) { // Búsqueda general si searchBy es nulo/vacío
                if (searchTerm.matches("[a-fA-F0-9]{24}")) {
                    orCriteriaList.add(Criteria.where("_id").is(searchTerm));
                }
                orCriteriaList.add(Criteria.where("email").regex(searchTerm, "i"));
                orCriteriaList.add(Criteria.where("nombre").regex(searchTerm, "i"));
                orCriteriaList.add(Criteria.where("user_name").regex(searchTerm, "i"));

                if (!orCriteriaList.isEmpty()) {
                    query.addCriteria(new Criteria().orOperator(orCriteriaList.toArray(new Criteria[0])));
                }
            }
        }

        // 1. Obtener el CONTEO TOTAL de elementos que coinciden con los filtros (SIN paginación)
        long count = mongoTemplate.count(query, User.class);

        // 2. Aplicar paginación y ordenación a la MISMA query para obtener la lista de la página actual
        query.with(pageable); // Modifica la query original añadiendo skip, limit y sort

        List<User> users = mongoTemplate.find(query, User.class);

        return new PageImpl<>(users, pageable, count);
    }
}
