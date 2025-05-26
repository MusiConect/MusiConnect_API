package com.api.musiconnect.model.entity;

import com.api.musiconnect.model.enums.MusicGenreEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "music_genres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MusicGenre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long generoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private MusicGenreEnum nombre;
}
