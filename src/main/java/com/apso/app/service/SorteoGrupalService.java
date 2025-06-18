package com.apso.app.service;

import com.apso.app.model.Estudiante;
import com.apso.app.model.SorteoGrupal;
import com.apso.app.model.Usuario;
import com.apso.app.repository.SorteoGrupalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SorteoGrupalService {

    private final SorteoGrupalRepository sorteoGrupalRepository;

    public SorteoGrupal guardarSorteo(String titulo, List<Estudiante> estudiantes, int numeroGrupos, String resultado, Usuario usuario) {
        SorteoGrupal sorteo = new SorteoGrupal();
        sorteo.setTitulo(titulo);
        sorteo.setFechaHora(LocalDateTime.now());
        sorteo.setCantidadGrupos(numeroGrupos);
        sorteo.setResultado(resultado);
        sorteo.setEstudiantes(estudiantes);
        sorteo.setUsuario(usuario);
        return sorteoGrupalRepository.save(sorteo);
    }

    public List<SorteoGrupal> obtenerSorteosPorUsuario(Usuario usuario) {
        return sorteoGrupalRepository.findByUsuarioOrderByFechaHoraDesc(usuario);
    }
}
