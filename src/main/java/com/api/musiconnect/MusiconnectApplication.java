package com.api.musiconnect;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.api.musiconnect.model.entity.Role;
import com.api.musiconnect.model.entity.MusicGenre;
import com.api.musiconnect.model.enums.MusicGenreEnum;
import com.api.musiconnect.model.enums.RoleEnum;
import com.api.musiconnect.repository.MusicGenreRepository;
import com.api.musiconnect.repository.RoleRepository;

@SpringBootApplication
public class MusiconnectApplication {

	public static void main(String[] args) {
		SpringApplication.run(MusiconnectApplication.class, args);
	}

	
    @Bean
    CommandLineRunner initMusicGenres(MusicGenreRepository musicGenreRepository) {
        return args -> {
            for (MusicGenreEnum genreEnum : MusicGenreEnum.values()) {
                if (!musicGenreRepository.existsByNombre(genreEnum)) {
                    musicGenreRepository.save(
                        MusicGenre.builder()
                            .nombre(genreEnum)
                            .build()
                    );
                }
            }
        };
    }

	@Bean
	CommandLineRunner initRoles(RoleRepository roleRepository) {
		return args -> {
			for (RoleEnum roleEnum : RoleEnum.values()) {
				if (!roleRepository.existsByName(roleEnum)) {
					roleRepository.save(
						Role.builder()
							.name(roleEnum)
							.build()
					);
				}
			}
		};
	}
}
