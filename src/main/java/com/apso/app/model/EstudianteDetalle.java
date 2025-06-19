package com.apso.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstudianteDetalle {
    private String id;
    private String nombre;
    private String email;
    private String grupoTeorico;
    private String asignatura;
    private String cargaId;
}
