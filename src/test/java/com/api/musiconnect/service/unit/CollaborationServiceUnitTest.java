package com.api.musiconnect.service.unit;

import com.api.musiconnect.dto.request.CollaborationRequest;
import com.api.musiconnect.dto.request.CollaborationUpdateRequest;
import com.api.musiconnect.dto.response.CollaborationResponse;
import com.api.musiconnect.dto.response.UserResponse;
import com.api.musiconnect.exception.BadRequestException;
import com.api.musiconnect.exception.BusinessRuleException;
import com.api.musiconnect.exception.ResourceNotFoundException;
import com.api.musiconnect.model.entity.Collaboration;
import com.api.musiconnect.model.entity.MusicGenre;
import com.api.musiconnect.model.entity.Role;
import com.api.musiconnect.model.entity.User;
import com.api.musiconnect.model.enums.CollaborationStatus;
import com.api.musiconnect.model.enums.MusicGenreEnum;
import com.api.musiconnect.model.enums.RoleEnum;
import com.api.musiconnect.repository.CollaborationRepository;
import com.api.musiconnect.repository.UserRepository;
import com.api.musiconnect.service.CollaborationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollaborationServiceUnitTest {

    @Mock
    private CollaborationRepository collaborationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CollaborationService collaborationService;

    // Test para crear colaboraciones

    @Test
    @DisplayName("CP01 - Crear colaboración válida")
    void createCollaboration_validData_success() {
        // Arrange
        CollaborationRequest request = new CollaborationRequest(
            "Colaboración Rock",
            "Colaboración entre guitarristas",
            LocalDate.now(),
            LocalDate.now().plusDays(5),
            1L
        );

        User usuario = new User();
        usuario.setDisponibilidad(true);
        usuario.setNombreArtistico("José C.");

        Collaboration saved = new Collaboration();
        saved.setUsuario(usuario);

        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(collaborationRepository.save(any())).thenReturn(saved);

        // Act
        CollaborationResponse result = collaborationService.crearColaboracion(request);

        // Assert
        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(collaborationRepository).save(any(Collaboration.class));
    }

    @Test
    @DisplayName("CP02 - Crear colaboración con fechas inválidas")
    void createCollaboration_invalidDates_throwsException() {
        // Arrange: fechaInicio posterior a fechaFin
        CollaborationRequest request = new CollaborationRequest(
            "Colaboración Inválida",
            "Fechas al revés",
            LocalDate.now().plusDays(10),
            LocalDate.now(),
            1L
        );

        // Act + Assert
        assertThrows(BadRequestException.class, () -> collaborationService.crearColaboracion(request));

        // Verifica que no se consultó el repositorio
        verifyNoInteractions(userRepository);
        verifyNoInteractions(collaborationRepository);
    }

    @Test
    @DisplayName("CP03 - Crear colaboración con título duplicado")
    void createCollaboration_duplicatedTitle_throwsException() {
        // Arrange
        String tituloDuplicado = "Jazz Nights";
        CollaborationRequest request = new CollaborationRequest(
                tituloDuplicado,
                "Sesiones de jazz semanales",
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                1L
        );

        when(collaborationRepository.existsByTituloIgnoreCase(tituloDuplicado))
                .thenReturn(true); // Simula que ya existe ese título

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () ->
                collaborationService.crearColaboracion(request)
        );

        assertEquals("Ya existe una colaboración con este título.", exception.getMessage());
        verify(collaborationRepository, never()).save(any(Collaboration.class)); // no debería guardar
    }

    @Test
    @DisplayName("CP04 - Crear colaboración con usuario no disponible")
    void createCollaboration_userNotAvailable_throwsException() {
        // Arrange
        CollaborationRequest request = new CollaborationRequest(
            "Colaboración restringida",
            "El usuario no está disponible",
            LocalDate.now(),
            LocalDate.now().plusDays(5),
            1L
        );

        User usuario = new User();
        usuario.setDisponibilidad(false); // Usuario NO disponible

        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // Act + Assert
        assertThrows(BusinessRuleException.class, () -> collaborationService.crearColaboracion(request));

        verify(userRepository).findById(1L);
        verify(collaborationRepository, never()).save(any(Collaboration.class));
    }

    @Test
    @DisplayName("CP05 - Crear colaboración con usuario inexistente")
    void createCollaboration_userNotFound_throwsException() {
        // Arrange
        CollaborationRequest request = new CollaborationRequest(
            "Colaboración sin usuario",
            "Intento con ID inválido",
            LocalDate.now(),
            LocalDate.now().plusDays(5),
            99L // ID que no existe
        );

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class, () -> collaborationService.crearColaboracion(request));

        verify(collaborationRepository).existsByTituloIgnoreCase("Colaboración sin usuario");
        verify(collaborationRepository, never()).save(any());
    }

    // Test para actualizar colaboraciones

    @Test
    @DisplayName("CP06 - Editar colaboración válida")
    void updateCollaboration_validData_success() {
        // Arrange
        Long collaborationId = 10L;

        CollaborationUpdateRequest request = new CollaborationUpdateRequest(
            1L, // usuarioId
            "Nuevo Título",
            "Descripción actualizada",
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(10),
            CollaborationStatus.EN_PROGRESO
        );

        User usuario = new User();
        usuario.setUserId(1L);

        Collaboration existente = new Collaboration();
        existente.setUsuario(usuario);

        when(collaborationRepository.findById(collaborationId)).thenReturn(Optional.of(existente));
        when(collaborationRepository.save(any())).thenReturn(existente);

        // Act
        Map<String, String> result = collaborationService.updateCollaboration(collaborationId, request);

        // Assert
        assertNotNull(result);
        assertEquals("Colaboración actualizada correctamente.", result.get("message"));
        verify(collaborationRepository).findById(collaborationId);
        verify(collaborationRepository).save(any(Collaboration.class));
    }

    @Test
    @DisplayName("CP07 - Editar colaboración no encontrada")
    void updateCollaboration_notFound_throwsException() {
        // Arrange
        Long collaborationId = 999L;

        CollaborationUpdateRequest request = new CollaborationUpdateRequest(
            1L,
            "Intento de edición",
            "Esta colaboración no existe",
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(6),
            CollaborationStatus.PENDIENTE
        );

        when(collaborationRepository.findById(collaborationId)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class, () -> collaborationService.updateCollaboration(collaborationId, request));

        verify(collaborationRepository).findById(collaborationId);
        verify(collaborationRepository, never()).save(any());
    }

    @Test
    @DisplayName("CP08 - Usuario sin permisos para editar la colaboración")
    void updateCollaboration_userNotOwner_throwsException() {
        // Arrange
        Long collaborationId = 20L;

        CollaborationUpdateRequest request = new CollaborationUpdateRequest(
            99L, // ID distinto al que creó la colaboración
            "Intento no autorizado",
            "Descripción no relevante",
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(10),
            CollaborationStatus.PENDIENTE
        );

        User propietarioReal = new User();
        propietarioReal.setUserId(1L); // Propietario real de la colaboración

        Collaboration existente = new Collaboration();
        existente.setUsuario(propietarioReal); // Asociación correcta

        when(collaborationRepository.findById(collaborationId)).thenReturn(Optional.of(existente));

        // Act + Assert
        assertThrows(BusinessRuleException.class, () -> collaborationService.updateCollaboration(collaborationId, request));

        verify(collaborationRepository).findById(collaborationId);
        verify(collaborationRepository, never()).save(any());
    }

    @Test
    @DisplayName("CP09 - Editar colaboración con fechas inválidas")
    void updateCollaboration_invalidDates_throwsException() {
        // Arrange
        Long collaborationId = 30L;

        CollaborationUpdateRequest request = new CollaborationUpdateRequest(
            1L,
            "Fechas mal",
            "Inicio posterior a fin",
            LocalDate.now().plusDays(10), // fechaInicio
            LocalDate.now().plusDays(5),  // fechaFin
            CollaborationStatus.PENDIENTE
        );

        User usuario = new User();
        usuario.setUserId(1L);

        Collaboration existente = new Collaboration();
        existente.setUsuario(usuario);

        when(collaborationRepository.findById(collaborationId)).thenReturn(Optional.of(existente));

        // Act + Assert
        assertThrows(BusinessRuleException.class, () -> collaborationService.updateCollaboration(collaborationId, request));

        verify(collaborationRepository).findById(collaborationId);
        verify(collaborationRepository, never()).save(any());
    }

    @Test
    @DisplayName("CP10 - Editar colaboración con estado inválido")
    void updateCollaboration_invalidStatus_throwsException() {
        // Arrange
        Long collaborationId = 40L;

        CollaborationUpdateRequest request = new CollaborationUpdateRequest(
            1L,
            "Estado inválido",
            "Probando un estado incorrecto",
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(10),
            null // Estado inválido: null
        );

        User usuario = new User();
        usuario.setUserId(1L);

        Collaboration existente = new Collaboration();
        existente.setUsuario(usuario);

        when(collaborationRepository.findById(collaborationId)).thenReturn(Optional.of(existente));

        // Act + Assert
        assertThrows(BusinessRuleException.class, () -> collaborationService.updateCollaboration(collaborationId, request));

        verify(collaborationRepository).findById(collaborationId);
        verify(collaborationRepository, never()).save(any());
    }

    // Test para listar colaboraciones activas

    @Test
    @DisplayName("CP11 - Listar colaboraciones activas exitosamente")
    void listarColaboracionesActivas_success() {
        // Arrange
        User usuario = new User();
        usuario.setNombreArtistico("José C.");

        Collaboration colaboracion1 = new Collaboration();
        colaboracion1.setEstado(CollaborationStatus.PENDIENTE);
        colaboracion1.setUsuario(usuario);

        Collaboration colaboracion2 = new Collaboration();
        colaboracion2.setEstado(CollaborationStatus.EN_PROGRESO);
        colaboracion2.setUsuario(usuario);

        when(collaborationRepository.findByEstadoIn(
            List.of(CollaborationStatus.PENDIENTE, CollaborationStatus.EN_PROGRESO)))
            .thenReturn(List.of(colaboracion1, colaboracion2));

        // Act
        List<CollaborationResponse> result = collaborationService.listarColaboracionesActivas();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(collaborationRepository).findByEstadoIn(
            List.of(CollaborationStatus.PENDIENTE, CollaborationStatus.EN_PROGRESO));
    }

    @Test
    @DisplayName("CP12 - No hay colaboraciones activas")
    void listarColaboracionesActivas_emptyList_throwsException() {
        // Arrange
        when(collaborationRepository.findByEstadoIn(
            List.of(CollaborationStatus.PENDIENTE, CollaborationStatus.EN_PROGRESO)))
            .thenReturn(List.of()); // lista vacía

        // Act + Assert
        assertThrows(BusinessRuleException.class, () -> collaborationService.listarColaboracionesActivas());

        verify(collaborationRepository).findByEstadoIn(
            List.of(CollaborationStatus.PENDIENTE, CollaborationStatus.EN_PROGRESO));
    }

    // Test para obtener todas las colaboraciones

    @Test
    @DisplayName("CP13 - Obtener todas las colaboraciones exitosamente")
    void getAllCollaborations_success() {
        // Arrange
        User usuario = new User();
        usuario.setNombreArtistico("José C.");

        Collaboration colaboracion1 = new Collaboration();
        colaboracion1.setUsuario(usuario);

        Collaboration colaboracion2 = new Collaboration();
        colaboracion2.setUsuario(usuario);

        when(collaborationRepository.findAll()).thenReturn(List.of(colaboracion1, colaboracion2));

        // Act
        List<CollaborationResponse> result = collaborationService.getAllCollaborations();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(collaborationRepository).findAll();
    }

    @Test
    @DisplayName("CP14 - No hay colaboraciones registradas")
    void getAllCollaborations_emptyList_returnsEmpty() {
        // Arrange
        when(collaborationRepository.findAll()).thenReturn(List.of());

        // Act
        List<CollaborationResponse> result = collaborationService.getAllCollaborations();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(collaborationRepository).findAll();
    }

    // Test para obtener colaboraciones por nombre artístico

    @Test
    @DisplayName("CP15 - Nombre artístico válido con resultados")
    void getByNombreArtistico_withResults_returnsList() {
        User usuario = new User();
        usuario.setNombreArtistico("José C.");

        Collaboration c1 = new Collaboration();
        c1.setUsuario(usuario);

        when(collaborationRepository.findByUsuario_NombreArtisticoIgnoreCase("José C.")).thenReturn(List.of(c1));

        List<CollaborationResponse> result = collaborationService.getByNombreArtistico("José C.");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(collaborationRepository).findByUsuario_NombreArtisticoIgnoreCase("José C.");
    }

    @Test
    @DisplayName("CP16 - Nombre artístico válido sin resultados")
    void getByNombreArtistico_noResults_returnsEmptyList() {
        when(collaborationRepository.findByUsuario_NombreArtisticoIgnoreCase("Desconocido")).thenReturn(List.of());

        List<CollaborationResponse> result = collaborationService.getByNombreArtistico("Desconocido");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(collaborationRepository).findByUsuario_NombreArtisticoIgnoreCase("Desconocido");
    }

    @Test
    @DisplayName("CP17 - Nombre artístico es null")
    void getByNombreArtistico_null_throwsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> collaborationService.getByNombreArtistico(null));

        assertEquals("El nombre artístico es obligatorio.", exception.getMessage());
    }

    @Test
    @DisplayName("CP18 - Nombre artístico es vacío o espacios")
    void getByNombreArtistico_emptyOrBlank_throwsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> collaborationService.getByNombreArtistico("   "));

        assertEquals("El nombre artístico es obligatorio.", exception.getMessage());
    }

    // Test para agregar colaboradores a una colaboración

    @Test
    @DisplayName("CP19 - Agregar colaborador exitosamente por nombre artístico")
    void addColaborador_success() {
        Collaboration colaboracion = new Collaboration();
        colaboracion.setColaboradores(new ArrayList<>());

        User user = new User();
        user.setUserId(2L);
        user.setDisponibilidad(true);

        User creador = new User();
        creador.setUserId(1L);
        colaboracion.setUsuario(creador);

        when(collaborationRepository.findById(1L)).thenReturn(Optional.of(colaboracion));
        when(userRepository.findByNombreArtisticoIgnoreCase("JoseArt"))
                .thenReturn(Optional.of(user));

        Map<String, String> result = collaborationService.addColaborador(1L, "JoseArt");

        assertEquals("Colaborador añadido correctamente", result.get("message"));
        verify(collaborationRepository).save(colaboracion);
    }

    @Test
    @DisplayName("CP20 - Colaboración no encontrada")
    void addColaborador_collaborationNotFound() {
        when(collaborationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> collaborationService.addColaborador(1L, "JoseArt"));
    }

    @Test
    @DisplayName("CP21 - Usuario no encontrado por nombre artístico")
    void addColaborador_userNotFound() {
        Collaboration colaboracion = new Collaboration();
        when(collaborationRepository.findById(1L)).thenReturn(Optional.of(colaboracion));
        when(userRepository.findByNombreArtisticoIgnoreCase("JoseArt"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> collaborationService.addColaborador(1L, "JoseArt"));
    }

    @Test
    @DisplayName("CP22 - Usuario no disponible para colaborar")
    void addColaborador_userNotAvailable() {
        Collaboration colaboracion = new Collaboration();
        User user = new User();
        user.setDisponibilidad(false);

        when(collaborationRepository.findById(1L)).thenReturn(Optional.of(colaboracion));
        when(userRepository.findByNombreArtisticoIgnoreCase("JoseArt"))
                .thenReturn(Optional.of(user));

        assertThrows(BusinessRuleException.class,
                () -> collaborationService.addColaborador(1L, "JoseArt"));
    }

    @Test
    @DisplayName("CP23 - Usuario ya es colaborador")
    void addColaborador_userAlreadyCollaborator() {
        User user = new User();
        user.setUserId(2L);
        user.setDisponibilidad(true);

        Collaboration colaboracion = new Collaboration();
        colaboracion.setColaboradores(new ArrayList<>(List.of(user)));

        User creador = new User();
        creador.setUserId(1L); 
        colaboracion.setUsuario(creador);

        when(collaborationRepository.findById(1L)).thenReturn(Optional.of(colaboracion));
        when(userRepository.findByNombreArtisticoIgnoreCase("JoseArt"))
                .thenReturn(Optional.of(user));

        assertThrows(BusinessRuleException.class,
                () -> collaborationService.addColaborador(1L, "JoseArt"));
    }

    @Test
    @DisplayName("CP24 - Usuario es el creador de la colaboración")
    void addColaborador_userIsCreator() {
        User user = new User();
        user.setUserId(2L);
        user.setDisponibilidad(true);

        Collaboration colaboracion = new Collaboration();
        colaboracion.setColaboradores(new ArrayList<>());
        colaboracion.setUsuario(user);

        when(collaborationRepository.findById(1L)).thenReturn(Optional.of(colaboracion));
        when(userRepository.findByNombreArtisticoIgnoreCase("JoseArt"))
                .thenReturn(Optional.of(user));

        assertThrows(BusinessRuleException.class,
                () -> collaborationService.addColaborador(1L, "JoseArt"));
    }

    @Test
    @DisplayName("CP25 - Nombre artístico es null")
    void addColaborador_nombreArtisticoNull() {
        // Arrange
        Collaboration colaboracion = new Collaboration();
        colaboracion.setUsuario(new User()); // opcionalmente simular creador
        colaboracion.setColaboradores(new ArrayList<>());

        when(collaborationRepository.findById(1L)).thenReturn(Optional.of(colaboracion));

        // Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> collaborationService.addColaborador(1L, null));

        verify(collaborationRepository).findById(1L);
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("CP26 - Nombre artístico es vacío o espacios")
    void addColaborador_nombreArtisticoVacio() {
        // Arrange
        Collaboration colaboracion = new Collaboration();
        colaboracion.setUsuario(new User());
        colaboracion.setColaboradores(new ArrayList<>());

        when(collaborationRepository.findById(1L)).thenReturn(Optional.of(colaboracion));

        // Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> collaborationService.addColaborador(1L, "   "));

        verify(collaborationRepository).findById(1L);
        verifyNoInteractions(userRepository);
    }

// Test para eliminar colaboraciones

    @Test
    @DisplayName("CP27 - Eliminación exitosa por el creador")
    void deleteCollaboration_success() {
        User creador = new User();
        creador.setUserId(1L);

        Collaboration colaboracion = new Collaboration();
        colaboracion.setUsuario(creador);

        when(collaborationRepository.findById(10L)).thenReturn(Optional.of(colaboracion));

        Map<String, String> response = collaborationService.deleteCollaboration(10L, 1L);

        assertEquals("Colaboración eliminada correctamente", response.get("message"));
        verify(collaborationRepository).deleteById(10L);
    }

    @Test
    @DisplayName("CP28 - collaborationId es null")
    void deleteCollaboration_nullCollaborationId() {
        assertThrows(IllegalArgumentException.class,
                () -> collaborationService.deleteCollaboration(null, 1L));
    }

    @Test
    @DisplayName("CP29 - userId es null")
    void deleteCollaboration_nullUserId() {
        assertThrows(IllegalArgumentException.class,
                () -> collaborationService.deleteCollaboration(10L, null));
    }

    @Test
    @DisplayName("CP30 - Colaboración no encontrada")
    void deleteCollaboration_notFound() {
        when(collaborationRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> collaborationService.deleteCollaboration(10L, 1L));
    }

    @Test
    @DisplayName("CP31 - Usuario no es el creador")
    void deleteCollaboration_userNotCreator() {
        User creador = new User();
        creador.setUserId(99L);

        Collaboration colaboracion = new Collaboration();
        colaboracion.setUsuario(creador);

        when(collaborationRepository.findById(10L)).thenReturn(Optional.of(colaboracion));

        assertThrows(BusinessRuleException.class,
                () -> collaborationService.deleteCollaboration(10L, 1L));
    }

    @Test
    @DisplayName("CP32 - El método getUsuario() devuelve null")
    void deleteCollaboration_usuarioNull() {
        Collaboration colaboracion = new Collaboration();
        colaboracion.setUsuario(null);

        when(collaborationRepository.findById(10L)).thenReturn(Optional.of(colaboracion));

        assertThrows(NullPointerException.class,
                () -> collaborationService.deleteCollaboration(10L, 1L));
    }

    @Test
    @DisplayName("CP33 - Obtener colaboración por ID existente")
    void getById_success() {
        // Arrange
        User creador = new User();
        creador.setUserId(1L);
        creador.setNombreArtistico("Artista Ejemplo");

        Collaboration colaboracion = new Collaboration();
        colaboracion.setColaboracionId(1L);
        colaboracion.setTitulo("Colaboración Test");
        colaboracion.setDescripcion("Descripción");
        colaboracion.setFechaInicio(LocalDate.now());
        colaboracion.setFechaFin(LocalDate.now().plusDays(10));
        colaboracion.setEstado(CollaborationStatus.PENDIENTE);
        colaboracion.setUsuario(creador); // Asignar el usuario al creador

        when(collaborationRepository.findById(1L)).thenReturn(Optional.of(colaboracion));

        // Act
        CollaborationResponse response = collaborationService.getById(1L);

        // Assert
        assertNotNull(response);
        assertEquals("Colaboración Test", response.titulo());
        verify(collaborationRepository).findById(1L);
    }

    @Test
    @DisplayName("CP34 - Obtener colaboración por ID inexistente")
    void getById_notFound() {
        when(collaborationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> collaborationService.getById(99L));
        verify(collaborationRepository).findById(99L);
    }

    @Test
    @DisplayName("CP35 - Listar colaboradores de colaboración existente")
    void listarColaboradores_success() {
        // Arrange
        User colaborador = new User();
        colaborador.setUserId(2L);
        colaborador.setNombreArtistico("Artista 2");
        colaborador.setEmail("artista2@mail.com");
        colaborador.setInstrumentos("Guitarra");
        colaborador.setBio("Bio del colaborador");
        colaborador.setUbicacion("Ciudad X");
        colaborador.setDisponibilidad(true);
        
        // Asignar rol simulado
        Role role = new Role();
        role.setName(RoleEnum.MUSICO);
        colaborador.setRole(role);

        // Asignar género musical simulado
        MusicGenre genero = new MusicGenre();
        genero.setNombre(MusicGenreEnum.ROCK);
        colaborador.setGenerosMusicales(List.of(genero));

        Collaboration colaboracion = new Collaboration();
        colaboracion.setColaboracionId(1L);
        colaboracion.setColaboradores(List.of(colaborador));

        when(collaborationRepository.findById(1L)).thenReturn(Optional.of(colaboracion));

        // Act
        List<UserResponse> responses = collaborationService.listarColaboradores(1L);

        // Assert
        assertEquals(1, responses.size());
        assertEquals("Artista 2", responses.get(0).nombreArtistico());
        verify(collaborationRepository).findById(1L);
    }

    @Test
    @DisplayName("CP36 - Listar colaboradores de colaboración inexistente")
    void listarColaboradores_notFound() {
        when(collaborationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> collaborationService.listarColaboradores(99L));
        verify(collaborationRepository).findById(99L);
    }

    @Test
    @DisplayName("CP37 - Listar estados de colaboración")
    void listarEstadosColaboracion_success() {
        List<String> estados = collaborationService.listarEstadosColaboracion();
        assertEquals(Arrays.asList("PENDIENTE", "EN_PROGRESO", "FINALIZADO"), estados);
    }
} 
