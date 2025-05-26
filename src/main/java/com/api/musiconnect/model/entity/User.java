package com.api.musiconnect.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;

    @NotBlank
    @Size(min = 4, max = 50)
    private String nombreArtistico;

    @NotBlank
    private String instrumentos;

    @Size(max = 300)
    private String bio;

    private String ubicacion;

    @NotNull
    private Boolean disponibilidad;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToMany
    @JoinTable(
        name = "user_music_genre",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "genero_id")
    )
    private List<MusicGenre> generosMusicales;

    @ManyToMany(mappedBy = "miembros")
    private List<Band> bandas;

}
