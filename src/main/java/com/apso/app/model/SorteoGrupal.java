package com.apso.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sorteos_grupales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SorteoGrupal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100, nullable = false)
    private String titulo;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "cantidad_grupos", nullable = false)
    private Integer cantidadGrupos;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String resultado;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "sorteo_estudiantes",
        joinColumns = @JoinColumn(name = "sorteo_id"),
        inverseJoinColumns = @JoinColumn(name = "estudiante_id")
    )
    private List<Estudiante> estudiantes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;
}
