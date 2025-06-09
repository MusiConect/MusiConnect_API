package com.api.musiconnect.service.unit;

import com.api.musiconnect.dto.request.ConvocationRequest;
import com.api.musiconnect.dto.request.ConvocationUpdateRequest;
import com.api.musiconnect.dto.response.ConvocationResponse;
import com.api.musiconnect.exception.BusinessRuleException;
import com.api.musiconnect.exception.ResourceNotFoundException;
import com.api.musiconnect.mapper.ConvocationMapper;
import com.api.musiconnect.model.entity.Convocation;
import com.api.musiconnect.model.entity.ConvocationFavorite;
import com.api.musiconnect.model.entity.User;
import com.api.musiconnect.repository.ConvocationFavoriteRepository;
import com.api.musiconnect.repository.ConvocationRepository;
import com.api.musiconnect.repository.UserRepository;
import com.api.musiconnect.service.ConvocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ConvocationServiceUnitTest {

    @InjectMocks
    private ConvocationService convocationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConvocationRepository convocationRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("CP01 - Crear Convocation exitosamente")
    void createConvocation_validUser_returnsResponse() {
        User user = new User();
        user.setUserId(1L);
        user.setDisponibilidad(true);

        ConvocationRequest request = new ConvocationRequest(
                1L,
                "Título",
                "Descripción",
                LocalDate.now().plusDays(5)
        );

        Convocation convocation = new Convocation();
        ConvocationResponse response = new ConvocationResponse(
                1L,
                "Título",
                "Descripción",
                LocalDate.now().plusDays(5),
                "UsuarioPrueba",
                "Convocatoria creada exitosamente."
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(convocationRepository.save(any())).thenReturn(convocation);

        try (var mocked = mockStatic(ConvocationMapper.class)) {
            mocked.when(() -> ConvocationMapper.toEntity(request, user)).thenReturn(convocation);
            mocked.when(() -> ConvocationMapper.toResponse(convocation)).thenReturn(response);

            ConvocationResponse result = convocationService.crearConvocatoria(request);

            assertNotNull(result);
            assertEquals(response, result);
        }
    }

    @Test
    @DisplayName("CP02 - Usuario no encontrado")
    void createConvocation_userNotFound_throwsException() {
        ConvocationRequest request = new ConvocationRequest(
                99L,
                "Título",
                "Descripción",
                LocalDate.now().plusDays(5)
        );
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> convocationService.crearConvocatoria(request));
    }

    @Test
    @DisplayName("CP03 - Usuario no disponible")
    void createConvocation_userUnavailable_throwsException() {
        User user = new User();
        user.setUserId(1L);
        user.setDisponibilidad(false);

        ConvocationRequest request = new ConvocationRequest(
                1L,
                "Título",
                "Descripción",
                LocalDate.now().plusDays(5)
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(BusinessRuleException.class, () -> convocationService.crearConvocatoria(request));
    }

    @Test
    @DisplayName("CP04 - Fecha límite en el pasado")
    void createConvocation_pastDeadline_throwsException() {
        User user = new User();
        user.setUserId(1L);
        user.setDisponibilidad(true);

        ConvocationRequest request = new ConvocationRequest(
                1L,
                "Título",
                "Descripción",
                LocalDate.now().minusDays(1)
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(BusinessRuleException.class, () -> convocationService.crearConvocatoria(request));
    }

    @Test
    @DisplayName("CP05 - Editar Convocation exitosamente")
    void editConvocation_validRequest_successfulUpdate() {
        Convocation convocation = new Convocation();
        convocation.setUsuario(new User());
        convocation.getUsuario().setUserId(1L);
        convocation.setFechaLimite(LocalDate.now().plusDays(2));

        ConvocationUpdateRequest updateRequest = new ConvocationUpdateRequest(
                1L,
                "Nuevo Título",
                "Nueva descripción",
                LocalDate.now().plusDays(5)
        );

        when(convocationRepository.findById(1L)).thenReturn(Optional.of(convocation));

        Map<String, String> result = convocationService.editarConvocatoria(1L, updateRequest);

        assertEquals("Convocatoria actualizada correctamente.", result.get("message"));
        assertEquals("Nuevo Título", convocation.getTitulo());
        assertEquals("Nueva descripción", convocation.getDescripcion());
    }

    @Test
    @DisplayName("CP06 - Editar Convocation con ID no encontrado")
    void editConvocation_idNotFound_throwsException() {
        ConvocationUpdateRequest updateRequest = new ConvocationUpdateRequest(
                1L, "Título", "Descripción", LocalDate.now().plusDays(2)
        );

        when(convocationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> convocationService.editarConvocatoria(1L, updateRequest));
    }

    @Test
    @DisplayName("CP07 - Editar Convocation por usuario no autorizado")
    void editConvocation_wrongUser_throwsException() {
        Convocation convocation = new Convocation();
        convocation.setUsuario(new User());
        convocation.getUsuario().setUserId(99L);
        convocation.setFechaLimite(LocalDate.now().plusDays(3));

        ConvocationUpdateRequest updateRequest = new ConvocationUpdateRequest(
                1L, "Título", "Descripción", LocalDate.now().plusDays(4)
        );

        when(convocationRepository.findById(1L)).thenReturn(Optional.of(convocation));

        assertThrows(BusinessRuleException.class, () -> convocationService.editarConvocatoria(1L, updateRequest));
    }

    @Test
    @DisplayName("CP08 - Editar Convocation ya finalizada")
    void editConvocation_pastDeadline_throwsException() {
        Convocation convocation = new Convocation();
        convocation.setUsuario(new User());
        convocation.getUsuario().setUserId(1L);
        convocation.setFechaLimite(LocalDate.now().minusDays(1));

        ConvocationUpdateRequest updateRequest = new ConvocationUpdateRequest(
                1L, "Título", "Descripción", LocalDate.now().plusDays(2)
        );

        when(convocationRepository.findById(1L)).thenReturn(Optional.of(convocation));

        assertThrows(BusinessRuleException.class, () -> convocationService.editarConvocatoria(1L, updateRequest));
    }

    @Test
    @DisplayName("CP12 - Listar convocatorias activas correctamente")
    void listarConvocatoriasActivas_conResultados_retornaLista() {
        Convocation conv1 = new Convocation();
        conv1.setActiva(true);

        Convocation conv2 = new Convocation();
        conv2.setActiva(true);

        List<Convocation> activas = List.of(conv1, conv2);

        ConvocationResponse res1 = mock(ConvocationResponse.class);
        ConvocationResponse res2 = mock(ConvocationResponse.class);

        when(convocationRepository.findAll()).thenReturn(activas);

        try (MockedStatic<ConvocationMapper> mocked = mockStatic(ConvocationMapper.class)) {
            mocked.when(() -> ConvocationMapper.toResponse(conv1)).thenReturn(res1);
            mocked.when(() -> ConvocationMapper.toResponse(conv2)).thenReturn(res2);

            List<ConvocationResponse> result = convocationService.listarConvocatoriasActivas();

            assertEquals(2, result.size());
            assertTrue(result.containsAll(List.of(res1, res2)));
        }
    }

    @Test
    @DisplayName("CP13 - Listar convocatorias activas sin resultados lanza excepción")
    void listarConvocatoriasActivas_sinResultados_lanzaExcepcion() {
        Convocation inactiva = new Convocation();
        inactiva.setActiva(false);

        when(convocationRepository.findAll()).thenReturn(List.of(inactiva));

        assertThrows(BusinessRuleException.class, () -> convocationService.listarConvocatoriasActivas());
    }

    @Test
    @DisplayName("CP14 - Listar convocatorias favoritas activas del usuario")
    void listarConvocatoriasFavoritas_usuarioConFavoritosActivos_retornaLista() {
        Long userId = 1L;

        User user = new User();
        user.setUserId(userId);

        Convocation conv1 = new Convocation();
        conv1.setActiva(true);
        Convocation conv2 = new Convocation();
        conv2.setActiva(true);

        ConvocationFavorite fav1 = new ConvocationFavorite();
        fav1.setConvocatoria(conv1);
        ConvocationFavorite fav2 = new ConvocationFavorite();
        fav2.setConvocatoria(conv2);

        ConvocationResponse res1 = mock(ConvocationResponse.class);
        ConvocationResponse res2 = mock(ConvocationResponse.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(convocationFavoriteRepository.findByUsuarioUserId(userId)).thenReturn(List.of(fav1, fav2));

        try (MockedStatic<ConvocationMapper> mocked = mockStatic(ConvocationMapper.class)) {
            mocked.when(() -> ConvocationMapper.toResponse(conv1)).thenReturn(res1);
            mocked.when(() -> ConvocationMapper.toResponse(conv2)).thenReturn(res2);

            List<ConvocationResponse> result = convocationService.listarConvocatoriasFavoritasPorUsuario(userId);

            assertEquals(2, result.size());
            assertTrue(result.containsAll(List.of(res1, res2)));
        }
    }

    @Test
    @DisplayName("CP15 - Usuario no encontrado al listar favoritas")
    void listarConvocatoriasFavoritas_usuarioNoExiste_lanzaExcepcion() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> convocationService.listarConvocatoriasFavoritasPorUsuario(99L));
    }

    @Test
    @DisplayName("CP16 - Usuario sin convocatorias favoritas activas")
    void listarConvocatoriasFavoritas_sinActivas_lanzaExcepcion() {
        Long userId = 2L;
        User user = new User();
        user.setUserId(userId);

        Convocation inactiva = new Convocation();
        inactiva.setActiva(false);
        ConvocationFavorite fav = new ConvocationFavorite();
        fav.setConvocatoria(inactiva);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(convocationFavoriteRepository.findByUsuarioUserId(userId)).thenReturn(List.of(fav));

        assertThrows(BusinessRuleException.class, () -> convocationService.listarConvocatoriasFavoritasPorUsuario(userId));
    }


}



