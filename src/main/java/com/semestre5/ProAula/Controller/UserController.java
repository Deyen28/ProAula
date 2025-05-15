package com.semestre5.ProAula.Controller;

import com.semestre5.ProAula.Model.Barrios;
import com.semestre5.ProAula.Model.Contaminante;
import com.semestre5.ProAula.Model.Reportes;
import com.semestre5.ProAula.Model.User;


import com.semestre5.ProAula.Services.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private BarriosService barriosService;
    @Autowired
    private ContaminanteService contaminanteService;
    @Autowired
    private ReportesService reportesService;
    @Autowired
    private StorageService storageService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/registerView")
    public String mostararFormulario(Model model) {
        model.addAttribute("user", new User());
        return "registerView";
    }
    @PostMapping("/registrar")
    public String registrar(@ModelAttribute("user") User usuario, Model model) {
        usuario.setUserTipo(User.UserTipo.NORMAL);
        userService.guardar(usuario);
        return "redirect:/LoginView";
    }

    @GetMapping("/LoginView")
    public String loginView(Model model) {
        model.addAttribute("user", new User());
        return "LoginView";
    }

    @PostMapping("/loguear")
    public String login(@ModelAttribute("user") User usuario, Model model, HttpSession session) {
        User user = userService.obtenerPorEmail(usuario.getEmail());
        if (user != null && user.getContrasena().equals(usuario.getContrasena())) {
            session.setAttribute("userId", user.getId());
            session.setAttribute("userType", user.getUserTipo());

            switch (user.getUserTipo()) {
                case ADMIN:
                    return "redirect:/admin/dashboard";
                case ENTIDAD:
                    return "redirect:/privileged/dashboard";
                case NORMAL:
                default:
                    return "redirect:/user/dashboard";
            }
        } else {
            model.addAttribute("error", "Email o contraseña incorrectos");
            return "LoginView";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
    // User

    @GetMapping("/user/dashboard")
    public String mostrarUserDashboard(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/LoginView";
        }
        String username = authentication.getName();
        Object principal = authentication.getPrincipal();
        String userFullName = "Usuario";
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            User currentUser = userService.obtenerPorEmail(username);
            if(currentUser != null){
                userFullName = currentUser.getNombre();
                model.addAttribute("currentUser", currentUser);
            } else {
                System.out.println("Advertencia: Usuario '" + username + "' autenticado pero no encontrado en la base de datos.");
            }
        } else {
            username = principal.toString();
        }
        model.addAttribute("pageTitle", "Dashboard de Usuario"); // Ejemplo: título de la página
        model.addAttribute("username", username); // Añade el nombre de usuario/email
        model.addAttribute("userFullName", userFullName); // Añade el nombre completo obtenido
        return "userIndex";
    }

    @GetMapping("/user/dashboard/userReportView")
    public String userReportView(Model model, Authentication authentication) {
        UserDetails userDetails = null;
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                userDetails = (UserDetails) principal;
            } else {
                System.err.println("Principal no es UserDetails: " + principal.toString());
                return "redirect:/error";
            }
        } else {
            return "redirect:/LoginView";
        }

        String username = userDetails.getUsername();
        User user = userService.obtenerPorEmail(username);
        if (user == null) {
            System.err.println("Usuario autenticado no encontrado en DB: " + username);
            return "redirect:/error";
        }
        String userId = user.getId();

        List<Reportes> reportesDelUsuario = reportesService.obtenerReportesPorUsuario(userId);
        List<Barrios> todosLosBarrios = barriosService.listarTodosLosBarrios();
        List<Contaminante> todosLosContaminantes = contaminanteService.listarTodosLosContaminantes();

        Map<String, String> mapaNombresBarrios = todosLosBarrios.stream()
                .collect(Collectors.toMap(Barrios::getId, Barrios::getNombre, (n1, n2) -> n1)); // Evita duplicados

        Map<String, String> mapaNombresContaminantes = todosLosContaminantes.stream()
                .collect(Collectors.toMap(Contaminante::getId, Contaminante::getNombre, (n1, n2) -> n1)); // Evita duplicados

        List<Map<String, Object>> reportesParaVista = new ArrayList<>();
        for (Reportes reporte : reportesDelUsuario) {
            Map<String, Object> datosVista = new HashMap<>();
            datosVista.put("reporte", reporte);

            String nombreBarrio = mapaNombresBarrios.getOrDefault(reporte.getBarrioId(), "ID Barrio Desconocido");
            datosVista.put("barrioNombre", nombreBarrio);

            List<String> nombresContaminantes = new ArrayList<>();
            if (reporte.getContaminantesIds() != null) {
                for (String contId : reporte.getContaminantesIds()) {
                    nombresContaminantes.add(mapaNombresContaminantes.getOrDefault(contId, "ID Cont. Desconocido"));
                }
            }
            datosVista.put("contaminanteNombres", nombresContaminantes);

            reportesParaVista.add(datosVista);
        }
        model.addAttribute("user", user);
        model.addAttribute("barrios", todosLosBarrios);
        model.addAttribute("contaminantes", todosLosContaminantes);
        model.addAttribute("reporte", new Reportes());
        model.addAttribute("reportesParaVista", reportesParaVista);

        return "userReportes";
    }

    @PostMapping("/Reportar")
    public String registrarReporte(
            @ModelAttribute Reportes reporte,
            @RequestParam("evidenciaFile") MultipartFile evidenciaFile,
            RedirectAttributes redirectAttributes, Authentication authentication) {

        UserDetails userDetails = null;
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetails) {
            userDetails = (UserDetails) authentication.getPrincipal();
        } else {
            redirectAttributes.addFlashAttribute("error", "Error de autenticación al crear reporte.");
            return "redirect:/user/dashboard/userReportView";
        }

        String username = userDetails.getUsername();
        User user = userService.obtenerPorEmail(username);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado en la base de datos.");
            return "redirect:/user/dashboard/userReportView";
        }

        reporte.setUserId(user.getId());
        reporte.setFechaReporte(LocalDate.now());
        reporte.setEstado(Reportes.EstadoReporte.PENDIENTE);

        if (evidenciaFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El archivo de evidencia es obligatorio.");
            return "redirect:/user/dashboard/userReportView";
        }
        if (reporte.getContaminantesIds() == null || reporte.getContaminantesIds().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Debe seleccionar al menos un tipo de contaminación.");
            return "redirect:/user/dashboard/userReportView";
        }
        if (reporte.getBarrioId() == null || reporte.getBarrioId().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Debe seleccionar un barrio.");
            return "redirect:/user/dashboard/userReportView";
        }

        try {
            reportesService.guardar(reporte, evidenciaFile);
            redirectAttributes.addFlashAttribute("success", "Reporte registrado exitosamente");
        } catch (Exception e) {
            System.err.println("Error al guardar reporte: " + e.getMessage());
            if (e instanceof IOException) {
                redirectAttributes.addFlashAttribute("error", "Error al procesar el archivo de evidencia: " + e.getMessage());
            } else {
                redirectAttributes.addFlashAttribute("error", "Error al registrar el reporte: " + e.getMessage());
            }
        }
        return "redirect:/user/dashboard/userReportView";
    }

    @GetMapping("/user/getReport/{id}")
    @ResponseBody
    public ResponseEntity<?> obtenerReporteJson(@PathVariable String id) {
        Reportes reporte = reportesService.obtenerPorId(id);
        if (reporte == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Reporte no encontrado con ID: " + id);
        }
        return ResponseEntity.ok(reporte);
    }


    @PostMapping("/user/updateReport/{id}")
    public String actualizarReporte(
            @PathVariable String id,
            @ModelAttribute Reportes reporteData,
            @RequestParam(value = "evidenciaFile", required = false) MultipartFile evidenciaFile,
            RedirectAttributes redirectAttributes, Authentication authentication) {
        UserDetails userDetails = null;
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetails) {
            userDetails = (UserDetails) authentication.getPrincipal();
        } else {
            redirectAttributes.addFlashAttribute("error", "Error de autenticación al actualizar reporte.");
            return "redirect:/user/dashboard/userReportView";
        }
        String username = userDetails.getUsername();
        User currentUser = userService.obtenerPorEmail(username);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado en la base de datos.");
            return "redirect:/user/dashboard/userReportView";
        }

        Reportes existingReporte = reportesService.obtenerPorId(id);
        if (existingReporte == null) {
            redirectAttributes.addFlashAttribute("error", "Reporte no encontrado con ID: " + id);
            return "redirect:/user/dashboard/userReportView";
        }
        if (!existingReporte.getUserId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tiene permiso para editar este reporte.");
            return "redirect:/user/dashboard/userReportView";
        }

        existingReporte.setDireccion(reporteData.getDireccion());
        existingReporte.setDescripcion(reporteData.getDescripcion());
        existingReporte.setBarrioId(reporteData.getBarrioId());
        existingReporte.setContaminantesIds(reporteData.getContaminantesIds());


        try {
            String oldEvidenceFilename = existingReporte.getEvidencia();
            String newEvidenceFilename = oldEvidenceFilename;

            if (evidenciaFile != null && !evidenciaFile.isEmpty()) {
                newEvidenceFilename = storageService.store(evidenciaFile);
                existingReporte.setEvidencia(newEvidenceFilename);
                if (oldEvidenceFilename != null && !oldEvidenceFilename.isEmpty() && !oldEvidenceFilename.equals(newEvidenceFilename)) {
                    try {
                        storageService.delete(oldEvidenceFilename);
                    } catch (IOException e) {
                        System.err.println("Advertencia: No se pudo eliminar la evidencia antigua: " + oldEvidenceFilename + " - " + e.getMessage());
                    }
                }
            }
            reportesService.actualizar(existingReporte);
            redirectAttributes.addFlashAttribute("success", "Reporte actualizado exitosamente");
        } catch (Exception e) {
            System.err.println("Error al actualizar reporte: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el reporte: " + e.getMessage());
        }

        return "redirect:/user/dashboard/userReportView";
    }

    @PostMapping("/user/deleteReport/{id}")
    public String eliminarUserReport(@PathVariable String id, RedirectAttributes redirectAttributes, Authentication authentication) {

        UserDetails userDetails = null;
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof UserDetails) {
            userDetails = (UserDetails) authentication.getPrincipal();
        } else {
            redirectAttributes.addFlashAttribute("error", "Error de autenticación al eliminar reporte.");
            return "redirect:/user/dashboard/userReportView";
        }
        String username = userDetails.getUsername();
        User currentUser = userService.obtenerPorEmail(username);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado en la base de datos.");
            return "redirect:/user/dashboard/userReportView";
        }
        Reportes reporte = reportesService.obtenerPorId(id);
        if (reporte == null) {
            redirectAttributes.addFlashAttribute("error", "Reporte no encontrado con ID: " + id);
            return "redirect:/user/dashboard/userReportView";
        }

        if (!reporte.getUserId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "No tiene permiso para eliminar este reporte.");
            return "redirect:/user/dashboard/userReportView";
        }
        try {
            String evidenceFilename = reporte.getEvidencia();
            reportesService.eliminar(id);
            if (evidenceFilename != null && !evidenceFilename.isEmpty()) {
                try {
                    storageService.delete(evidenceFilename);
                } catch (IOException e) {
                    System.err.println("Advertencia: No se pudo eliminar el archivo de evidencia: " + evidenceFilename + " - " + e.getMessage());
                }
            }
            redirectAttributes.addFlashAttribute("success", "Reporte eliminado exitosamente");
        } catch (Exception e) {
            System.err.println("Error al eliminar reporte: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el reporte: " + e.getMessage());
        }
        return "redirect:/user/dashboard/userReportView";
    }

    @GetMapping("/user/dashboard/userEditView")
    public String userEditView(Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error de autenticación.");
            return "redirect:/LoginView";
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        User user = userService.obtenerPorEmail(username);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado.");
            return "redirect:/logout";
        }
        user.setContrasena("");
        model.addAttribute("user", user);
        return "editUser";
    }

    @PostMapping("/ActualizarUser")
    public String actualizarUser(
            @ModelAttribute("user") User userFromForm,
            @RequestParam(value = "nuevaContrasena", required = false) String nuevaContrasena,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetails)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: Sesión inválida.");
            return "redirect:/LoginView";
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        User existingUser = userService.obtenerPorEmail(username);
        if (existingUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: Usuario no encontrado.");
            return "redirect:/logout";
        }
        if (!existingUser.getId().equals(userFromForm.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: Intento de actualizar un usuario incorrecto.");
            return "redirect:/user/dashboard/userEditView";
        }
        existingUser.setNombre(userFromForm.getNombre());
        existingUser.setUser_name(userFromForm.getUser_name());
        existingUser.setDireccion(userFromForm.getDireccion());
        if (nuevaContrasena != null && !nuevaContrasena.trim().isEmpty()) {
            existingUser.setContrasena(passwordEncoder.encode(nuevaContrasena));
            System.out.println("Contraseña actualizada para usuario: " + username);
        } else {
            System.out.println("Contraseña NO actualizada para usuario: " + username);
        }
        try {
            userService.actualizarUsuario(existingUser);
            redirectAttributes.addFlashAttribute("successMessage", "¡Perfil actualizado exitosamente!");
            System.out.println("Perfil actualizado para usuario: " + username);
        } catch (Exception e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el perfil: " + e.getMessage());
        }
        return "redirect:/user/dashboard/userEditView";
    }

    // ADMIN

    @GetMapping("/privileged/dashboard")
    public ResponseEntity<String> privilegedDashboard() {
        return ResponseEntity.ok("Privileged Dashboard");
    }


}
