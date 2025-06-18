package com.apso.app.repository;

import com.apso.app.model.SorteoGrupal;
import com.apso.app.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SorteoGrupalRepository extends JpaRepository<SorteoGrupal, Long> {
    List<SorteoGrupal> findByUsuarioOrderByFechaHoraDesc(Usuario usuario);
}
