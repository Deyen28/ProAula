package com.semestre5.ProAula.Controller;
import com.semestre5.ProAula.Model.*;
import com.semestre5.ProAula.Repository.InformeAlertaRepository;
import com.semestre5.ProAula.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;
    @Autowired
    private ReportesService reportesService;
    @Autowired
    private BarriosService barriosService;
    @Autowired
    private ContaminanteService contaminanteService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private StorageService storageService;
    @Autowired
    private InformeAlertaService informeAlertaService;


    @Value("${powerbi.analytics.embed.url}") // Define esta propiedad en application.properties
    private String powerBiEmbedUrl;

    @GetMapping("/dashboard")
    public String adminView(Model model,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int size,
                            @RequestParam(required = false) String searchTerm,
                            @RequestParam(required = false) String searchBy, // "id" o "email"
                            @RequestParam(defaultValue = "id,asc") String[] sort) {
        String sortField = sort[0];
        String sortDirection = (sort.length > 1 && "desc".equalsIgnoreCase(sort[1])) ? "desc" : "asc";

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));
        Page<User> paginaUsuarios = userService.findUsuariosPaginadosYFiltrados(searchTerm, searchBy, pageable);

        model.addAttribute("paginaUsuarios", paginaUsuarios);
        model.addAttribute("usuarios", paginaUsuarios.getContent()); // Para compatibilidad con el th:each existente
        model.addAttribute("currentPage", paginaUsuarios.getNumber());
        model.addAttribute("totalPages", paginaUsuarios.getTotalPages());
        model.addAttribute("totalItems", paginaUsuarios.getTotalElements());

        // Pasar parámetros de búsqueda y ordenación de vuelta para mantenerlos en los enlaces de paginación/filtros
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("searchBy", searchBy);
        model.addAttribute("sortField", sortField);
        model.addAttribute("sortDir", sortDirection);
        model.addAttribute("reverseSortDir", sortDirection.equals("asc") ? "desc" : "asc");
        return "adminView";
    }

    @GetMapping("/getUser/{id}")
    @ResponseBody
    public ResponseEntity<?> getUserJson(@PathVariable String id) {
        User user = userService.obtenerPorId(id);
        if (user != null) {
            user.setContrasena(null);
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }
    }

    @PostMapping("/updateUser/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateUserFromAdmin(
            @PathVariable String id,
            @RequestParam String nombre,
            @RequestParam String user_name,
            @RequestParam String email,
            @RequestParam String direccion,
            @RequestParam String userTipo
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            User existingUser = userService.obtenerPorId(id);
            if (existingUser == null) {
                response.put("success", false);
                response.put("error", "Usuario no encontrado.");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            existingUser.setNombre(nombre);
            existingUser.setUser_name(user_name);
            existingUser.setEmail(email);
            existingUser.setDireccion(direccion);
            try {
                User.UserTipo newUserType = User.UserTipo.valueOf(userTipo.toUpperCase());
                existingUser.setUserTipo(newUserType);
            } catch (IllegalArgumentException e) {
                response.put("success", false);
                response.put("error", "Tipo de usuario inválido: " + userTipo);
                return ResponseEntity.badRequest().body(response);
            }
            userService.actualizarUsuario(existingUser);
            response.put("success", true);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    @PostMapping("/deleteUser/{id}")
    public String deleteUserFromAdmin(@PathVariable String id, Authentication authentication, RedirectAttributes redirectAttributes) {
        String adminUsername = authentication.getName();
        User adminUser = userService.obtenerPorEmail(adminUsername);
        User userToDelete = userService.obtenerPorId(id);

        if (userToDelete == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Usuario a eliminar no encontrado.");
            return "redirect:/admin/dashboard";
        }
        if (adminUser != null && adminUser.getId().equals(userToDelete.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "No puedes eliminar tu propia cuenta de administrador.");
            return "redirect:/admin/dashboard";
        }
        try {
            userService.eliminarUsuario(id);
            redirectAttributes.addFlashAttribute("successMessage", "Usuario eliminado correctamente.");
        } catch (Exception e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el usuario: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/userReports/{userId}")
    public String showUserReportsForAdmin(@PathVariable String userId, Model model, RedirectAttributes redirectAttributes) {
        User user = userService.obtenerPorId(userId);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado.");
            return "redirect:/admin/dashboard";
        }
        if (reportesService == null || barriosService == null || contaminanteService == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Servicios de reportes no disponibles.");
            return "redirect:/admin/dashboard";
        }

        List<Reportes> reportesDelUsuario = reportesService.obtenerReportesPorUsuario(userId);
        List<Barrios> todosLosBarrios = barriosService.listarTodosLosBarrios();
        List<Contaminante> todosLosContaminantes = contaminanteService.listarTodosLosContaminantes();

        Map<String, String> mapaNombresBarrios = todosLosBarrios.stream()
                .collect(Collectors.toMap(Barrios::getId, Barrios::getNombre, (n1, n2) -> n1));
        Map<String, String> mapaNombresContaminantes = todosLosContaminantes.stream()
                .collect(Collectors.toMap(Contaminante::getId, Contaminante::getNombre, (n1, n2) -> n1));

        List<Map<String, Object>> reportesParaVista = new ArrayList<>();
        for (Reportes reporte : reportesDelUsuario) {
            Map<String, Object> datosVista = new HashMap<>();
            datosVista.put("reporte", reporte);
            String nombreBarrio = mapaNombresBarrios.getOrDefault(reporte.getBarrioId(), "ID Desconocido");
            datosVista.put("barrioNombre", nombreBarrio);
            List<String> nombresContaminantes = new ArrayList<>();
            if (reporte.getContaminantesIds() != null) {
                for (String contId : reporte.getContaminantesIds()) {
                    nombresContaminantes.add(mapaNombresContaminantes.getOrDefault(contId, "ID Desconocido"));
                }
            }
            datosVista.put("contaminanteNombres", nombresContaminantes);
            reportesParaVista.add(datosVista);
        }
        model.addAttribute("targetUser", user);
        model.addAttribute("reportesParaVista", reportesParaVista);

        return "adminRepoUser";
    }

    @GetMapping("/profile")
    public String showAdminProfile(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Acceso no autorizado.");
            return "redirect:/LoginView";
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String adminUsername = userDetails.getUsername(); // Email del admin
        User adminUser = userService.obtenerPorEmail(adminUsername);
        if (adminUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Administrador no encontrado.");
            return "redirect:/logout";
        }

        adminUser.setContrasena("");
        model.addAttribute("user", adminUser);
        return "adminProfile";
    }

    @PostMapping("/profile/update")
    public String updateAdminProfile(
            @ModelAttribute("user") User userFromForm,
            @RequestParam(value = "nuevaContrasena", required = false) String nuevaContrasena,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: Sesión inválida.");
            return "redirect:/LoginView";
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String adminUsername = userDetails.getUsername();

        User existingAdmin = userService.obtenerPorEmail(adminUsername);

        if (existingAdmin == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: Administrador no encontrado.");
            return "redirect:/logout";
        }

        if (!existingAdmin.getId().equals(userFromForm.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: Intento de actualizar un perfil incorrecto.");
            return "redirect:/admin/profile";
        }
        existingAdmin.setNombre(userFromForm.getNombre());
        existingAdmin.setUser_name(userFromForm.getUser_name());
        existingAdmin.setDireccion(userFromForm.getDireccion());

        if (nuevaContrasena != null && !nuevaContrasena.trim().isEmpty()) {
            existingAdmin.setContrasena(passwordEncoder.encode(nuevaContrasena));
            System.out.println("Contraseña actualizada para admin: " + adminUsername);
        } else {
            System.out.println("Contraseña NO actualizada para admin: " + adminUsername);
        }
        try {
            userService.actualizarUsuario(existingAdmin);
            redirectAttributes.addFlashAttribute("successMessage", "Perfil actualizado correctamente!");
            System.out.println("Perfil actualizado para admin: " + adminUsername);
        } catch (Exception e) {
            System.err.println("Error al actualizar perfil de admin: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el perfil: " + e.getMessage());
        }
        return "redirect:/admin/profile";
    }

    @GetMapping("/reportes")
    public String showAllReports(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 @RequestParam(required = false) String usuarioTerm,
                                 @RequestParam(required = false) String contaminanteId,
                                 @RequestParam(required = false) String barrioId,
                                 @RequestParam(required = false) Reportes.EstadoReporte estadoReporte,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
                                 @RequestParam(defaultValue = "fechaReporte,desc") String[] sort) {

        System.out.println("ADMIN_REPORTS_DEBUG: Entrando a /admin/reportes (con lógica optimizada)");
        try {
            String sortField = sort[0];
            String sortDirection = (sort.length > 1 && "asc".equalsIgnoreCase(sort[1])) ? "asc" : "desc";
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(sortDirection), sortField));

            List<String> userIdsCoincidentes = null;
            boolean searchTermIsLikelyAnId = false;

            if (StringUtils.hasText(usuarioTerm)) {
                if (usuarioTerm.matches("[a-fA-F0-9]{24}")) {
                    searchTermIsLikelyAnId = true;
                } else {
                    Page<User> usuariosEncontrados = userService.findUsuariosPaginadosYFiltrados(usuarioTerm, null, PageRequest.of(0, 50));
                    if (!usuariosEncontrados.isEmpty()) {
                        userIdsCoincidentes = usuariosEncontrados.getContent().stream().map(User::getId).collect(Collectors.toList());
                    } else {
                        userIdsCoincidentes = new ArrayList<>();
                    }
                }
            }

            Page<Reportes> paginaReportes = reportesService.findAllReportesPaginadosYFiltrados(
                    userIdsCoincidentes, searchTermIsLikelyAnId, usuarioTerm,
                    contaminanteId, barrioId, estadoReporte, fechaDesde, fechaHasta, pageable
            );

            List<Map<String, Object>> reportesEnriquecidos = enriquecerListaDeReportes(paginaReportes.getContent());

            model.addAttribute("paginaReportes", paginaReportes);
            model.addAttribute("reportesParaVistaGlobal", reportesEnriquecidos);
            model.addAttribute("currentPage", paginaReportes.getNumber());
            model.addAttribute("totalPages", paginaReportes.getTotalPages());
            model.addAttribute("totalItems", paginaReportes.getTotalElements());

            // --- CAMBIO CRÍTICO AQUÍ ---
            // Comenta o elimina esta línea para evitar cargar todos los usuarios:
            // model.addAttribute("usuarios", userService.listarTodos());
            // Si tu plantilla adminReportes.html tiene un <select> para usuarios que depende de esta lista,
            // necesitarás comentarlo o eliminarlo también en el HTML.

            // Estos pueden quedarse si las listas de contaminantes y barrios no son excesivamente grandes.
            // Si también son enormes, considera la misma estrategia para ellos.
            model.addAttribute("contaminantes", contaminanteService.listarTodosLosContaminantes());
            model.addAttribute("barrios", barriosService.listarTodosLosBarrios());
            model.addAttribute("estadosReporte", Reportes.EstadoReporte.values());

            model.addAttribute("usuarioTermFiltro", usuarioTerm);
            model.addAttribute("contaminanteIdFiltro", contaminanteId);
            model.addAttribute("barrioIdFiltro", barrioId);
            model.addAttribute("estadoReporteFiltro", estadoReporte);
            model.addAttribute("fechaDesdeFiltro", fechaDesde);
            model.addAttribute("fechaHastaFiltro", fechaHasta);
            model.addAttribute("sortField", sortField);
            model.addAttribute("sortDir", sortDirection);
            model.addAttribute("reverseSortDir", sortDirection.equals("asc") ? "desc" : "asc");

            System.out.println("ADMIN_REPORTS_DEBUG: Devolviendo vista adminReportes (con lógica optimizada y sin carga masiva de usuarios para filtros)");
            return "adminReportes";

        } catch (Exception e) {
            System.err.println("ADMIN_REPORTS_EXCEPTION (con lógica optimizada): " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error crítico al cargar reportes de admin: " + e.getMessage());
            return "error";
        }
    }

    private List<Map<String, Object>> enriquecerListaDeReportes(List<Reportes> reportesDePaginaActual) {
        if (reportesDePaginaActual == null || reportesDePaginaActual.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Recolectar IDs únicos de la página actual de reportes
        List<String> userIdsEnPagina = reportesDePaginaActual.stream()
                .map(Reportes::getUserId)
                .filter(Objects::nonNull) // Filtrar IDs nulos si es posible que existan
                .distinct()
                .collect(Collectors.toList());

        List<String> barrioIdsEnPagina = reportesDePaginaActual.stream()
                .map(Reportes::getBarrioId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        List<String> contaminanteIdsEnPagina = reportesDePaginaActual.stream()
                .filter(r -> r.getContaminantesIds() != null) // Asegurarse que la lista de IDs no sea nula
                .flatMap(r -> r.getContaminantesIds().stream())
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // 2. Obtener solo las entidades necesarias usando los IDs recolectados
        Map<String, String> mapaNombresUsuarios = userService.findUsersByIds(userIdsEnPagina).stream()
                .collect(Collectors.toMap(User::getId, User::getUser_name, (n1, n2) -> n1));

        Map<String, String> mapaNombresBarrios = barriosService.findBarriosByIds(barrioIdsEnPagina).stream()
                .collect(Collectors.toMap(Barrios::getId, Barrios::getNombre, (n1, n2) -> n1));

        Map<String, String> mapaNombresContaminantes = contaminanteService.findContaminantesByIds(contaminanteIdsEnPagina).stream()
                .collect(Collectors.toMap(Contaminante::getId, Contaminante::getNombre, (n1, n2) -> n1));

        // 3. Enriquecer
        return reportesDePaginaActual.stream().map(reporte -> {
            Map<String, Object> datosVista = new HashMap<>();
            datosVista.put("reporte", reporte);
            datosVista.put("usuarioNombre", mapaNombresUsuarios.getOrDefault(reporte.getUserId(), "ID Usuario Desconocido"));
            datosVista.put("barrioNombre", mapaNombresBarrios.getOrDefault(reporte.getBarrioId(), "ID Barrio Desconocido"));

            List<String> nombresCont;
            if (reporte.getContaminantesIds() != null) {
                nombresCont = reporte.getContaminantesIds().stream()
                        .map(id -> mapaNombresContaminantes.getOrDefault(id, "ID Cont. Desconocido"))
                        .collect(Collectors.toList());
            } else {
                nombresCont = Collections.emptyList();
            }
            datosVista.put("contaminanteNombres", nombresCont);
            return datosVista;
        }).collect(Collectors.toList());
    }

    @PostMapping("/reportes/updateEstado")
    @ResponseBody
    public ResponseEntity<?> updateReporteEstado(
            @RequestParam String reporteId,
            @RequestParam Reportes.EstadoReporte nuevoEstado) {
        try {
            Reportes reporteActualizado = reportesService.actualizarEstadoReporte(reporteId, nuevoEstado);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Estado del reporte actualizado a " + reporteActualizado.getEstado().name());
            response.put("nuevoEstado", reporteActualizado.getEstado().name());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }



    @PostMapping("/deleteReport/{id}")
    public String deleteAnyReport(@PathVariable String id, RedirectAttributes redirectAttributes) {
        Reportes reporte = reportesService.obtenerPorId(id);
        if (reporte == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Reporte no encontrado con ID: " + id);
            return "redirect:/admin/reportes";
        }
        try {
            String evidenceFilename = reporte.getEvidencia();

            reportesService.eliminar(id);

            if (evidenceFilename != null && !evidenceFilename.isEmpty()) {
                try {
                    storageService.delete(evidenceFilename);
                } catch (IOException e) {
                    System.err.println("Advertencia: No se pudo eliminar archivo de evidencia: " + evidenceFilename + " - " + e.getMessage());
                }
            }
            redirectAttributes.addFlashAttribute("successMessage", "Reporte eliminado exitosamente.");
        } catch (Exception e) {
            System.err.println("Error al eliminar reporte (admin): " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el reporte: " + e.getMessage());
        }
        return "redirect:/admin/reportes";
    }

    @GetMapping("/analytics")
    public String showAnalyticsPage(Model model) {
        model.addAttribute("powerBiEmbedUrl", powerBiEmbedUrl);
        model.addAttribute("pageTitle", "Dashboard de Analíticas"); // Título opcional para la página
        return "adminAnalytics";
    }


    /**
     * Muestra la página de gestión de Informes de Alerta.
     * Prepara datos para la tabla principal y para los filtros del modal de creación.
     */
    @GetMapping("/informes")
    public String showGestionInformesPage(Model model) {
        List<InformeAlerta> todosLosInformes = informeAlertaService.findAll();

        // Para la tabla principal, podríamos querer mostrar un resumen de los reportes asociados.
        // Esto puede ser complejo si hay muchos reportes. Por ahora, pasamos los informes tal cual.
        // Se podría añadir lógica para obtener un conteo o algunos detalles clave de los reportes asociados.
        model.addAttribute("informesExistentes", todosLosInformes);

        // Datos para el modal de NUEVO informe (incluyendo filtros para seleccionar reportes)
        model.addAttribute("informeNuevo", new InformeAlerta()); // Para th:object
        model.addAttribute("barrios", barriosService.listarTodosLosBarrios()); // Para el filtro de barrio de reportes
        model.addAttribute("contaminantes", contaminanteService.listarTodosLosContaminantes()); // Para el filtro de contaminante de reportes
        model.addAttribute("estadosReporte", Reportes.EstadoReporte.values()); // Para el filtro de estado de reportes

        // Enums para el formulario del InformeAlerta
        model.addAttribute("estadosInformeAlerta", InformeAlerta.EstadoInforme.values());
        model.addAttribute("valoracionesRiesgo", InformeAlerta.ValoracionRiesgo.values());

        return "adminInformes"; // Nombre del archivo adminInformes.html
    }

    /**
     * Endpoint AJAX para filtrar reportes que se mostrarán en el modal para selección.
     * Devuelve una lista de reportes "enriquecidos" con nombres.
     */
    @GetMapping("/api/reportes/filtrar")
    @ResponseBody
    public List<Map<String, Object>> getReportesFiltradosParaSeleccion(
            @RequestParam(required = false) String barrioId,
            @RequestParam(required = false) String contaminanteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            @RequestParam(required = false) Reportes.EstadoReporte estadoReporte) {

        List<Reportes> reportesFiltrados = reportesService.getReportesFiltrados(
                barrioId, contaminanteId, fechaDesde, fechaHasta, estadoReporte);

        // Enriquecer con nombres para la tabla de selección en el modal
        // (Reutilizando el método helper si lo tienes, o implementando la lógica aquí)
        return enriquecerListaDeReportes(reportesFiltrados);
    }

    /**
     * Guarda un nuevo Informe de Alerta o actualiza uno existente.
     * El objeto 'informe' se enlaza desde el formulario del modal.
     * Los 'reportesIds' se reciben del formulario si los checkboxes tienen name="reportesIds".
     */
    @PostMapping("/informes/guardar")
    public String saveOrUpdateInformeAlerta(
            @ModelAttribute("informeNuevo") InformeAlerta informe, // th:object del modal
            RedirectAttributes redirectAttributes) {
        try {
            // La lógica de fechaCreacion y asegurar listas no nulas está en el servicio save()
            informeAlertaService.save(informe);
            redirectAttributes.addFlashAttribute("successMessage", "Informe de alerta guardado correctamente.");
        } catch (Exception e) {
            System.err.println("Error al guardar informe de alerta: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error al guardar el informe: " + e.getMessage());
        }
        return "redirect:/admin/informes";
    }

    /**
     * Obtiene los datos de un Informe de Alerta específico para edición (JSON para AJAX).
     * Incluye los reportesIds para que el modal de edición pueda preseleccionar los reportes asociados.
     */
    @GetMapping("/informes/get/{id}")
    @ResponseBody
    public ResponseEntity<?> getInformeAlertaJson(@PathVariable String id) {
        InformeAlerta informe = informeAlertaService.findById(id);
        if (informe != null) {
            // Aquí podrías querer devolver un DTO que también incluya detalles de los reportes asociados
            // si el modal de edición necesita mostrar más que solo los IDs.
            // Por ahora, devolvemos el informe tal cual, que incluye reportesIds.
            return ResponseEntity.ok(informe);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Informe de alerta no encontrado con ID: " + id);
        }
    }

    /**
     * Elimina un Informe de Alerta.
     */
    @PostMapping("/informes/eliminar/{id}")
    public String deleteInformeAlerta(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            if (informeAlertaService.findById(id) == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Informe a eliminar no encontrado.");
                return "redirect:/admin/informes";
            }
            informeAlertaService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Informe de alerta eliminado correctamente.");
        } catch (Exception e) {
            System.err.println("Error al eliminar informe de alerta: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el informe: " + e.getMessage());
        }
        return "redirect:/admin/informes";
    }
}