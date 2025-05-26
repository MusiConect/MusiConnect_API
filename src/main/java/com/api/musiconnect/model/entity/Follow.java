package com.api.musiconnect.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.api.musiconnect.model.entity.User;
import com.api.musiconnect.model.entity.Band;


@Entity
@Table(name = "follows")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long followId;

    @ManyToOne
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower; // quien sigue

    @ManyToOne
    @JoinColumn(name = "followed_user_id")
    private User followedUser; // usuario seguido (opcional)

    @ManyToOne
    @JoinColumn(name = "followed_band_id")
    private Band followedBand; // banda seguida (opcional)

    private LocalDateTime fechaSeguimiento;
}
