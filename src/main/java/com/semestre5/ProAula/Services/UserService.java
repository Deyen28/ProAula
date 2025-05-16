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
        // Query para construir los criterios de filtro
        Query filterQuery = new Query();
        List<Criteria> criteriaList = new ArrayList<>(); // Lista para manejar múltiples criterios con OR

        if (StringUtils.hasText(searchTerm)) {
            if ("id".equalsIgnoreCase(searchBy)) {
                if (searchTerm.matches("[a-fA-F0-9]{24}")) { // Validar formato ObjectId
                    filterQuery.addCriteria(Criteria.where("_id").is(searchTerm));
                } else {
                    // Si se busca por ID pero no es un formato válido, no devolver nada
                    return new PageImpl<>(List.of(), pageable, 0);
                }
            } else if ("email".equalsIgnoreCase(searchBy)) {
                filterQuery.addCriteria(Criteria.where("email").regex(searchTerm, "i"));
            } else if (!StringUtils.hasText(searchBy)) { // Si searchBy está vacío, buscar en múltiples campos
                if (searchTerm.matches("[a-fA-F0-9]{24}")) {
                    criteriaList.add(Criteria.where("_id").is(searchTerm));
                }
                criteriaList.add(Criteria.where("email").regex(searchTerm, "i"));
                criteriaList.add(Criteria.where("nombre").regex(searchTerm, "i"));
                criteriaList.add(Criteria.where("user_name").regex(searchTerm, "i"));
                filterQuery.addCriteria(new Criteria().orOperator(criteriaList.toArray(new Criteria[0])));
            }
            // Si searchBy tiene un valor no reconocido y searchTerm existe, no se añaden criterios
            // (efectivamente no se filtra por searchTerm a menos que searchBy sea válido o nulo).
        }

        // 1. Obtener el CONTEO TOTAL de elementos que coinciden con los filtros (SIN paginación)
        long count = mongoTemplate.count(filterQuery, User.class);

        // 2. Aplicar paginación y ordenación a la consulta para obtener la lista de la página actual
        // Se crea una nueva Query para la paginación, aplicando los mismos criterios
        Query paginatedQuery = new Query();
        if (!filterQuery.getQueryObject().isEmpty()) { // Solo añadir criterios si existen
            paginatedQuery.addCriteria(Criteria.where("_id").exists(true).andOperator((Collection<Criteria>) filterQuery.getQueryObject()));
        }
        paginatedQuery.with(pageable); // Aplicar paginación y ordenación

        List<User> users = mongoTemplate.find(paginatedQuery, User.class);

        return new PageImpl<>(users, pageable, count);
    }
}
