package com.apso.app.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrupoDetalle {
    private String nombre;
    private List<EstudianteDetalle> estudiantes;
}
