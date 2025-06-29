package com.api.musiconnect.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "bands")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Band {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bandId;

    @NotBlank(message = "El nombre de la banda es obligatorio")
    @Size(min = 4, max = 50, message = "El nombre de la banda debe tener entre 4 y 50 caracteres.")
    @Column(unique = true)
    private String nombre;

    private String descripcion;

    private String estado;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private User administrador;

    @ManyToMany
    @JoinTable(
        name = "band_music_genre",
        joinColumns = @JoinColumn(name = "band_id"),
        inverseJoinColumns = @JoinColumn(name = "genero_id")
    )
    private List<MusicGenre> generosMusicales;

    @ManyToMany
    @JoinTable(
        name = "user_band",
        joinColumns = @JoinColumn(name = "band_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> miembros;

    @OneToMany(mappedBy = "followedBand", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private java.util.List<Follow> followers;
}
