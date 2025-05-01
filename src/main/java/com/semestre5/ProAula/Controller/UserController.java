package com.semestre5.ProAula.Controller;

import com.semestre5.ProAula.Model.Barrios;
import com.semestre5.ProAula.Model.Contaminante;
import com.semestre5.ProAula.Model.Reportes;
import com.semestre5.ProAula.Model.User;


import com.semestre5.ProAula.Services.BarriosService;
import com.semestre5.ProAula.Services.ContaminanteService;
import com.semestre5.ProAula.Services.ReportesService;
import com.semestre5.ProAula.Services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
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

    @GetMapping("/registerView")
    public String mostrarFormulario(Model model) {
        model.addAttribute("user", new User());
        return "registerView";
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@RequestBody User usuario) {
        try {
            usuario.setUserTipo(User.UserTipo.NORMAL);
            User savedUser = userService.guardar(usuario);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al registrar el usuario: " + e.getMessage());
        }
    }

    @GetMapping("/LoginView")
    public String loginView(Model model) {
        model.addAttribute("user", new User());
        return "LoginView";
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Sesión cerrada");
    }

    // User

    @GetMapping("/user/dashboard")
    public ResponseEntity<String> userDashboard() {
        return ResponseEntity.ok("User Dashboard");
    }

    @GetMapping("/user/dashboard/userReportView")
    public ResponseEntity<?> userReportView() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        User user = userService.obtenerPorEmail(username);
        String userId = user.getId();

        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        response.put("barrios", barriosService.listarTodosLosBarrios());
        response.put("contaminantes", contaminanteService.listarTodosLosContaminantes());
        response.put("reporte", new Reportes());

        List<Reportes> reportes = reportesService.obtenerReportesPorUsuario(userId);
        response.put("reportes", reportes);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/Reportar")
    public ResponseEntity<String> registrarReporte(
            @RequestBody Reportes reporte,
            @RequestParam("evidenciaFile") MultipartFile evidencia) {

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        User user = userService.obtenerPorEmail(username);

        reporte.setUserId(user.getId());

        System.out.println("Reporte recibido: " + reporte); // Log para depurar

        if (reporte.getContaminantesIds() != null && !reporte.getContaminantesIds().isEmpty()) {
            String primerContaminanteId = reporte.getContaminantesIds().get(0);
            System.out.println("Primer Contaminante ID: " + primerContaminanteId);

            // Obtener el objeto Contaminante completo (si es necesario)
            Contaminante contaminante = contaminanteService.obtenerPorId(primerContaminanteId);
            System.out.println("Contaminante Obtenido: " + contaminante);

            // Asignar el objeto Contaminante (si tu modelo lo soporta)
            // reporte.setContaminante(contaminante); // Si tienes el campo Contaminante en Reportes
        } else {
            System.out.println("No hay Contaminantes IDs en el reporte.");
        }

        if (reporte.getBarrioId() != null) {
            Barrios barrio = barriosService.obtenerPorId(reporte.getBarrioId());
            System.out.println("Barrio Obtenido: " + barrio);
            // reporte.setBarrio(barrio); // Si tienes el campo Barrio en Reportes
        } else {
            System.out.println("No hay Barrio ID en el reporte.");
        }

        reportesService.guardar(reporte, evidencia);

        return ResponseEntity.ok("Reporte registrado exitosamente");
    }

    @GetMapping("/user/getReport/{id}")
    public ResponseEntity<Map<String, Object>> obtenerReporte(@PathVariable String id) {
        Reportes reporte = reportesService.obtenerPorId(id);
        if (reporte == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> reporteDTO = new HashMap<>();

        reporteDTO.put("idReportes", reporte.getId());
        reporteDTO.put("direccion", reporte.getDireccion());
        reporteDTO.put("descripcion", reporte.getDescripcion());
        reporteDTO.put("contaminanteId", reporte.getContaminantesIds());
        reporteDTO.put("barrioId", reporte.getBarrioId());
        reporteDTO.put("evidencia", reporte.getEvidencia());

        return ResponseEntity.ok(reporteDTO);
    }

    @PutMapping("/user/updateReport/{id}")
    public ResponseEntity<String> actualizarReporte(
            @PathVariable String id,
            @RequestBody Reportes reporte, // Usar @RequestBody para JSON
            @RequestParam(value = "evidenciaFile", required = false) MultipartFile evidencia) {

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        User user = userService.obtenerPorEmail(username);

        Reportes existingReporte = reportesService.obtenerPorId(id);
        if (existingReporte == null) {
            return ResponseEntity.notFound().build();
        }

        existingReporte.setUserId(user.getId());
        existingReporte.setDireccion(reporte.getDireccion());
        existingReporte.setDescripcion(reporte.getDescripcion());
        existingReporte.setFechaReporte(reporte.getFechaReporte());
        existingReporte.setBarrioId(reporte.getBarrioId());
        existingReporte.setContaminantesIds(reporte.getContaminantesIds()); // Corrected line

        if (evidencia != null && !evidencia.isEmpty()) {
            reportesService.guardar(existingReporte, evidencia);
        } else {
            reportesService.actualizar(existingReporte);
        }

        return ResponseEntity.ok("Reporte actualizado exitosamente");
    }
    @DeleteMapping("/user/deleteReport/{id}")
    public ResponseEntity<String> eliminarUserReport(@PathVariable String id) {
        try {
            reportesService.eliminar(id);
            return ResponseEntity.ok("Reporte eliminado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar el reporte: " + e.getMessage());
        }
    }

    @GetMapping("/user/dashboard/userEditView")
    public ResponseEntity<?> userEditView() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        User user = userService.obtenerPorEmail(username);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/ActualizarUser")
    public ResponseEntity<String> actualizarUser(@RequestBody User updatedUser) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = userDetails.getUsername();
        User existingUser = userService.obtenerPorEmail(username);

        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }

        updatedUser.setId(existingUser.getId()); // Ensure the ID is set for updating

        try {
            User savedUser = userService.actualizarUsuario(updatedUser);
            return ResponseEntity.ok("Perfil actualizado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar el perfil: " + e.getMessage());
        }
    }

    // ADMIN

    @GetMapping("/admin/dashboard")
    public ResponseEntity<?> adminDashboard() {
        List<User> usuarios = userService.listarTodos();
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/privileged/dashboard")
    public ResponseEntity<String> privilegedDashboard() {
        return ResponseEntity.ok("Privileged Dashboard");
    }


}
