package com.apso.app.controller;

import com.apso.app.model.Estudiante;
import com.apso.app.model.SorteoGrupal;
import com.apso.app.model.Usuario;
import com.apso.app.repository.EstudianteRepository;
import com.apso.app.repository.SorteoGrupalRepository;
import com.apso.app.repository.UsuarioRepository;
import com.apso.app.service.PDFService;
import com.apso.app.model.GrupoDetalle;
import com.apso.app.model.EstudianteDetalle;
import java.util.regex.*;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class GrupoController {

    private final EstudianteRepository estudianteRepository;
    private final SorteoGrupalRepository sorteoGrupalRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    private PDFService pdfService;

    private List<List<Estudiante>> resultadoTemporal; // Guarda el sorteo no guardado
    private int cantidadGruposTemporal = 0;

    @GetMapping("/sorteogrupos")
    public String mostrarFormularioSorteo(@AuthenticationPrincipal OidcUser oidcUser, Model model) {
        agregarDatosUsuario(model, oidcUser);
        model.addAttribute("grupos", null); // Sin sorteo al inicio
        return "sorteogrupos";
    }

    @PostMapping("/sorteogrupos")
    public String procesarSorteo(@AuthenticationPrincipal OidcUser oidcUser,
                                 @RequestParam("cantidadGrupos") int cantidadGrupos,
                                 Model model) {
        agregarDatosUsuario(model, oidcUser);

        List<Estudiante> estudiantes = estudianteRepository.findAll();
        if (cantidadGrupos < 1 || cantidadGrupos > estudiantes.size() / 2) {
            model.addAttribute("error", "La cantidad de grupos debe estar entre 1 y " + (estudiantes.size() / 2));
            return "sorteogrupos";
        }

        Collections.shuffle(estudiantes);
        List<List<Estudiante>> grupos = new ArrayList<>();
        for (int i = 0; i < cantidadGrupos; i++) {
            grupos.add(new ArrayList<>());
        }

        int indexGrupo = 0;
        for (Estudiante estudiante : estudiantes) {
            grupos.get(indexGrupo).add(estudiante);
            indexGrupo = (indexGrupo + 1) % cantidadGrupos;
        }

        resultadoTemporal = grupos;
        cantidadGruposTemporal = cantidadGrupos;

        model.addAttribute("grupos", grupos);
        model.addAttribute("cantidadGrupos", cantidadGrupos);
        model.addAttribute("mostrarBotones", true);
        return "sorteogrupos";
    }
    /*
    @PostMapping("/sorteogrupos/guardar")
    public String guardarSorteo(@AuthenticationPrincipal OidcUser oidcUser,
                                @RequestParam("titulo") String titulo,
                                Model model) {
        agregarDatosUsuario(model, oidcUser);

        if (oidcUser == null || resultadoTemporal == null || resultadoTemporal.isEmpty()) {
            model.addAttribute("error", "No hay un sorteo para guardar.");
            return "sorteogrupos";
        }

        // Obtener o crear usuario desde Auth0
        String sub = oidcUser.getSubject();
        Usuario usuario = usuarioRepository.findByAuth0Id(sub)
                .orElseGet(() -> {
                    Usuario nuevo = new Usuario();
                    nuevo.setSub(sub);
                    nuevo.setNombre(oidcUser.getFullName());
                    nuevo.setEmail(oidcUser.getEmail());
                    return usuarioRepository.save(nuevo);
                });

        // Crear sorteo
        SorteoGrupal sorteo = new SorteoGrupal();
        sorteo.setTitulo(titulo);
        sorteo.setCantidadGrupos(cantidadGruposTemporal);
        sorteo.setFechaHora(LocalDateTime.now());
        sorteo.setUsuario(usuario);

        // Guardar el resultado como texto plano
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < resultadoTemporal.size(); i++) {
            sb.append("Grupo ").append(i + 1).append(":\n");
            for (Estudiante est : resultadoTemporal.get(i)) {
                sb.append("- ").append(est.getNombre()).append(" (").append(est.getEmail()).append(")\n");
            }
            sb.append("\n");
        }
        sorteo.setResultado(sb.toString());

        // Agregar estudiantes al sorteo
        List<Estudiante> todosLosEstudiantes = resultadoTemporal.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        sorteo.setEstudiantes(todosLosEstudiantes);

        // Guardar sorteo
        sorteoGrupalRepository.save(sorteo);

        model.addAttribute("mensaje", "Sorteo guardado exitosamente como: " + titulo);
        resultadoTemporal = null;
        cantidadGruposTemporal = 0;

        return "redirect:/sorteogrupos";
    }
        */    @PostMapping("/sorteogrupos/guardar")
    public String guardarSorteo(
            @AuthenticationPrincipal OidcUser oidcUser,
            @RequestParam("titulo") String titulo,
            RedirectAttributes redirectAttributes) {
        try {
            // Verificar autenticación
            if (oidcUser == null) {
                redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para guardar un sorteo.");
                return "redirect:/sorteogrupos";
            }

            // Validar que haya un sorteo temporal
            if (resultadoTemporal == null || resultadoTemporal.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "No hay un sorteo para guardar.");
                return "redirect:/sorteogrupos";
            }            // Obtener o crear usuario
            String auth0Id = oidcUser.getSubject();
            Usuario usuario = usuarioRepository.findByAuth0Id(auth0Id)
                .orElseGet(() -> {
                    Usuario nuevo = new Usuario();
                    nuevo.setAuth0Id(auth0Id);
                    nuevo.setNombre(oidcUser.getFullName());
                    nuevo.setEmail(oidcUser.getEmail());
                    return usuarioRepository.save(nuevo);
                });

            // Crear sorteo
            SorteoGrupal sorteo = new SorteoGrupal();
            sorteo.setTitulo(titulo);
            sorteo.setCantidadGrupos(cantidadGruposTemporal);
            sorteo.setFechaHora(LocalDateTime.now());
            sorteo.setUsuario(usuario);

            // Guardar el resultado como texto formateado
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < resultadoTemporal.size(); i++) {
                sb.append("Grupo ").append(i + 1).append(":\n");
                for (Estudiante est : resultadoTemporal.get(i)) {
                    sb.append("- ID: ").append(est.getId())
                      .append("\n  Nombre: ").append(est.getNombre())
                      .append("\n  Email: ").append(est.getEmail())
                      .append("\n  Grupo Teórico: ").append(est.getGrupoTeorico())
                      .append("\n  Asignatura: ").append(est.getAsignatura())
                      .append("\n  Carga ID: ").append(est.getCargaId())
                      .append("\n");
                }
                sb.append("\n");
            }
            sorteo.setResultado(sb.toString());

            // Agregar estudiantes al sorteo
            List<Estudiante> todosLosEstudiantes = resultadoTemporal.stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
            sorteo.setEstudiantes(todosLosEstudiantes);

            // Guardar sorteo
            sorteoGrupalRepository.save(sorteo);

            // Limpiar el sorteo temporal
            resultadoTemporal = null;
            cantidadGruposTemporal = 0;

            redirectAttributes.addFlashAttribute("mensaje", "Sorteo guardado exitosamente como: " + titulo);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar el sorteo: " + e.getMessage());
        }

        return "redirect:/sorteogrupos";
    }


    @PostMapping("/sorteogrupos/reiniciar")
    public String reiniciarSorteo(@AuthenticationPrincipal OidcUser oidcUser, Model model) {
        agregarDatosUsuario(model, oidcUser);
        resultadoTemporal = null;
        cantidadGruposTemporal = 0;
        model.addAttribute("mensaje", "Sorteo reiniciado.");
        return "sorteogrupos";
    }    @GetMapping("/historialsorteos")
    public String mostrarHistorialSorteos(@AuthenticationPrincipal OidcUser oidcUser, Model model) {
        // Verificar autenticación
        if (oidcUser == null) {
            model.addAttribute("error", "Debes iniciar sesión para ver el historial.");
            return "historialsorteos";
        }

        // Buscar usuario
        String auth0Id = oidcUser.getSubject();
        Usuario usuario = usuarioRepository.findByAuth0Id(auth0Id)
            .orElseGet(() -> {
                Usuario nuevo = new Usuario();
                nuevo.setAuth0Id(auth0Id);
                nuevo.setNombre(oidcUser.getFullName());
                nuevo.setEmail(oidcUser.getEmail());
                return usuarioRepository.save(nuevo);
            });

        // Obtener sorteos ordenados por fecha
        List<SorteoGrupal> sorteos = sorteoGrupalRepository.findByUsuarioOrderByFechaHoraDesc(usuario);
        model.addAttribute("sorteos", sorteos);

        // Agregar datos del usuario para la vista
        agregarDatosUsuario(model, oidcUser);

        return "historialsorteos";
    }

    @GetMapping("/historialsorteos/exportar/{id}")
    public ResponseEntity<byte[]> exportarSorteoPDF(@PathVariable Long id) {
        // Buscar el sorteo
        Optional<SorteoGrupal> sorteoOpt = sorteoGrupalRepository.findById(id);
        if (sorteoOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SorteoGrupal sorteo = sorteoOpt.get();
        
        // Generar el PDF
        byte[] pdf = pdfService.generarPDFSorteo(sorteo);

        // Normalizar el título del sorteo para usarlo como nombre de archivo
        String nombreArchivo = sorteo.getTitulo()
            .replaceAll("[^a-zA-Z0-9\\s-]", "") // Remover caracteres especiales
            .trim()
            .replaceAll("\\s+", "-") // Reemplazar espacios con guiones
            + ".pdf";

        // Configurar la respuesta HTTP
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename(nombreArchivo).build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdf);
    }

    @GetMapping("/grupos/exportar-pdf/{id}")
    public ResponseEntity<byte[]> exportarPDF(@PathVariable Long id) {
        try {
            // Obtener el sorteo de la base de datos
            Optional<SorteoGrupal> sorteoOpt = sorteoGrupalRepository.findById(id);
            if (!sorteoOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            
            SorteoGrupal sorteo = sorteoOpt.get();
            
            // Generar PDF
            byte[] pdfContent = pdfService.generarPDFSorteo(sorteo);
            
            // Limpiar el título para usarlo como nombre de archivo
            String nombreArchivo = sorteo.getTitulo()
                .replaceAll("[^a-zA-Z0-9.-]", "_") // Reemplazar caracteres no permitidos con _
                .replaceAll("_{2,}", "_") // Reemplazar múltiples _ consecutivos con uno solo
                .toLowerCase() + ".pdf";
            
            // Configurar headers para la descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                    .filename(nombreArchivo)
                    .build());
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfContent);
                
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/sorteo/{id}")
    public String verDetalleSorteo(@PathVariable Long id, @AuthenticationPrincipal OidcUser oidcUser, Model model) {
        if (oidcUser == null) {
            model.addAttribute("error", "Debes iniciar sesión para ver el detalle del sorteo.");
            return "detalle-sorteo";
        }
        Optional<SorteoGrupal> sorteoOpt = sorteoGrupalRepository.findById(id);
        if (sorteoOpt.isPresent()) {
            SorteoGrupal sorteo = sorteoOpt.get();
            model.addAttribute("sorteo", sorteo);
            // Procesar resultado a estructura de grupos
            List<GrupoDetalle> gruposDetalle = new ArrayList<>();
            String[] grupos = sorteo.getResultado().split("Grupo \\d+:");
            for (int i = 1; i < grupos.length; i++) {
                String nombreGrupo = "Grupo " + i;
                String estudiantesData = grupos[i];
                List<EstudianteDetalle> estudiantes = new ArrayList<>();
                Pattern pattern = Pattern.compile("- ID: (\\d+)\\s*\\n\\s*Nombre: ([^\\n]+)\\s*\\n\\s*Email: ([^\\n]+)\\s*\\n\\s*Grupo Teórico: ([^\\n]+)\\s*\\n\\s*Asignatura: ([^\\n]+)\\s*\\n\\s*Carga ID: (\\d+)");
                Matcher matcher = pattern.matcher(estudiantesData);
                while (matcher.find()) {
                    estudiantes.add(new EstudianteDetalle(
                        matcher.group(1),
                        matcher.group(2),
                        matcher.group(3),
                        matcher.group(4),
                        matcher.group(5),
                        matcher.group(6)
                    ));
                }
                gruposDetalle.add(new GrupoDetalle(nombreGrupo, estudiantes));
            }
            model.addAttribute("gruposDetalle", gruposDetalle);
        } else {
            model.addAttribute("sorteo", null);
        }
        agregarDatosUsuario(model, oidcUser);
        return "detalle-sorteo";
    }

    private void agregarDatosUsuario(Model model, OidcUser oidcUser) {
        if (oidcUser != null) {
            model.addAttribute("userName", oidcUser.getFullName());
            model.addAttribute("userEmail", oidcUser.getEmail());
        }
    }
}
