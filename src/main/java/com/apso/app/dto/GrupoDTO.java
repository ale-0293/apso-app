package com.apso.app.dto;

import lombok.Data;
import java.util.List;
import com.apso.app.model.Estudiante;

@Data
public class GrupoDTO {
    private int numero;
    private List<Estudiante> estudiantes;
}
