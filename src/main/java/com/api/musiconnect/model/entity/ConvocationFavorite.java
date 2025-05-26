package com.api.musiconnect.model.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "convocation_favorites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConvocationFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    @ManyToOne
    @JoinColumn(name = "convocatoria_id", nullable = false)
    private Convocation convocatoria;
}
