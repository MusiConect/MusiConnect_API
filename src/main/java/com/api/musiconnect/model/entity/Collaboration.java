package com.api.musiconnect.model.entity;
import com.api.musiconnect.model.enums.CollaborationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;




@Entity
@Table(name = "collaborations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Collaboration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long colaboracionId;

    @NotBlank
    @Size(min = 4, max = 50)
    private String titulo;


    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private User usuario;

    private String descripcion;

    @NotNull
    private LocalDate fechaInicio;

    @NotNull
    private LocalDate fechaFin;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CollaborationStatus estado;

    @ManyToMany
    @JoinTable(
        name = "collaboration_members",
        joinColumns = @JoinColumn(name = "collaboration_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> colaboradores = new ArrayList<>();
}
