package com.semestre5.ProAula.Controller;
import com.semestre5.ProAula.Model.Barrios;
import com.semestre5.ProAula.Model.Contaminante;
import com.semestre5.ProAula.Model.Reportes;
import com.semestre5.ProAula.Model.User;
import com.semestre5.ProAula.Services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Value("${powerbi.analytics.embed.url}") // Define esta propiedad en application.properties
    private String powerBiEmbedUrl;

    @GetMapping("/dashboard")
    public String adminView(Model model) {
        List<User> usuarios = userService.listarTodos();
        model.addAttribute("usuarios", usuarios);
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
    public String showAllReports(Model model) {
        List<Reportes> todosLosReportes = reportesService.listarTodos();
        List<User> todosLosUsuarios = userService.listarTodos();
        List<Barrios> todosLosBarrios = barriosService.listarTodosLosBarrios();
        List<Contaminante> todosLosContaminantes = contaminanteService.listarTodosLosContaminantes();

        Map<String, String> mapaNombresUsuarios = todosLosUsuarios.stream()
                .collect(Collectors.toMap(User::getId, User::getUser_name, (n1, n2) -> n1)); // Usa user_name

        Map<String, String> mapaNombresBarrios = todosLosBarrios.stream()
                .collect(Collectors.toMap(Barrios::getId, Barrios::getNombre, (n1, n2) -> n1));

        Map<String, String> mapaNombresContaminantes = todosLosContaminantes.stream()
                .collect(Collectors.toMap(Contaminante::getId, Contaminante::getNombre, (n1, n2) -> n1));

        List<Map<String, Object>> reportesParaVistaGlobal = new ArrayList<>();
        for (Reportes reporte : todosLosReportes) {
            Map<String, Object> datosVista = new HashMap<>();
            datosVista.put("reporte", reporte); // Objeto reporte original

            String nombreUsuario = mapaNombresUsuarios.getOrDefault(reporte.getUserId(), "Usuario Desconocido");
            datosVista.put("usuarioNombre", nombreUsuario);

            String nombreBarrio = mapaNombresBarrios.getOrDefault(reporte.getBarrioId(), "Barrio Desconocido");
            datosVista.put("barrioNombre", nombreBarrio);

            List<String> nombresContaminantes = new ArrayList<>();
            if (reporte.getContaminantesIds() != null) {
                for (String contId : reporte.getContaminantesIds()) {
                    nombresContaminantes.add(mapaNombresContaminantes.getOrDefault(contId, "ID Cont. Desconocido"));
                }
            }
            datosVista.put("contaminanteNombres", nombresContaminantes); // Lista de nombres
            reportesParaVistaGlobal.add(datosVista);
        }
        model.addAttribute("reportesParaVistaGlobal", reportesParaVistaGlobal);
        return "adminReportes";
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
}