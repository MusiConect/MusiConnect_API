package com.api.musiconnect.service.unit;

import com.api.musiconnect.dto.request.FollowRequest;
import com.api.musiconnect.dto.request.UnfollowRequest;
import com.api.musiconnect.dto.response.FollowResponse;
import com.api.musiconnect.dto.response.FollowedProfileResponse;
import com.api.musiconnect.exception.BusinessRuleException;
import com.api.musiconnect.exception.ResourceNotFoundException;
import com.api.musiconnect.model.entity.Band;
import com.api.musiconnect.model.entity.Follow;
import com.api.musiconnect.model.entity.User;
import com.api.musiconnect.repository.BandRepository;
import com.api.musiconnect.repository.FollowRepository;
import com.api.musiconnect.repository.UserRepository;
import com.api.musiconnect.service.FollowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.Map;
import java.util.List;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FollowServiceUnitTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BandRepository bandRepository;

    @InjectMocks
    private FollowService followService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("CP01 - Follower no existe")
    void crearFollow_followerNoExiste_exception() {
        FollowRequest request = new FollowRequest(1L, 2L, null);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> followService.crearFollow(request));
    }

    @Test
    @DisplayName("CP02 - No se proporciona ni usuario ni banda")
    void crearFollow_sinSeguido_exception() {
        FollowRequest request = new FollowRequest(1L, null, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));

        assertThrows(BusinessRuleException.class, () -> followService.crearFollow(request));
    }

    @Test
    @DisplayName("CP03 - Se proporciona usuario y banda al mismo tiempo")
    void crearFollow_ambosSeguidos_exception() {
        FollowRequest request = new FollowRequest(1L, 2L, 3L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));

        assertThrows(BusinessRuleException.class, () -> followService.crearFollow(request));
    }

    @Test
    @DisplayName("CP04 - Usuario intenta seguirse a sí mismo")
    void crearFollow_seguirseASiMismo_exception() {
        FollowRequest request = new FollowRequest(1L, 1L, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));

        assertThrows(BusinessRuleException.class, () -> followService.crearFollow(request));
    }

    @Test
    @DisplayName("CP05 - Ya existe seguimiento al usuario")
    void crearFollow_usuarioYaSeguido_exception() {
        FollowRequest request = new FollowRequest(1L, 2L, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(followRepository.existsByFollowerUserIdAndFollowedUserUserId(1L, 2L)).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> followService.crearFollow(request));
    }

    @Test
    @DisplayName("CP06 - Usuario seguido no existe")
    void crearFollow_usuarioSeguidoNoExiste_exception() {
        FollowRequest request = new FollowRequest(1L, 2L, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(followRepository.existsByFollowerUserIdAndFollowedUserUserId(1L, 2L)).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> followService.crearFollow(request));
    }

    @Test
    @DisplayName("CP07 - Usuario seguido no está disponible")
    void crearFollow_usuarioSeguidoNoDisponible_exception() {
        User seguido = new User();
        seguido.setDisponibilidad(false);
        FollowRequest request = new FollowRequest(1L, 2L, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(followRepository.existsByFollowerUserIdAndFollowedUserUserId(1L, 2L)).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(seguido));

        assertThrows(BusinessRuleException.class, () -> followService.crearFollow(request));
    }

    @Test
    @DisplayName("CP08 - Seguimiento a usuario exitoso")
    void crearFollow_usuario_exitoso() {
        User follower = new User();
        User seguido = new User();
        seguido.setDisponibilidad(true);
        Follow follow = Follow.builder().follower(follower).followedUser(seguido).fechaSeguimiento(LocalDateTime.now()).build();

        FollowRequest request = new FollowRequest(1L, 2L, null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(follower));
        when(followRepository.existsByFollowerUserIdAndFollowedUserUserId(1L, 2L)).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(seguido));
        when(followRepository.save(any(Follow.class))).thenReturn(follow);

        FollowResponse response = followService.crearFollow(request);
        assertNotNull(response);
    }

    @Test
    @DisplayName("CP09 - Ya existe seguimiento a la banda")
    void crearFollow_bandaYaSeguida_exception() {
        FollowRequest request = new FollowRequest(1L, null, 3L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(followRepository.existsByFollowerUserIdAndFollowedBandBandId(1L, 3L)).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> followService.crearFollow(request));
    }

    @Test
    @DisplayName("CP10 - Banda seguida no existe")
    void crearFollow_bandaNoExiste_exception() {
        FollowRequest request = new FollowRequest(1L, null, 3L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(followRepository.existsByFollowerUserIdAndFollowedBandBandId(1L, 3L)).thenReturn(false);
        when(bandRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> followService.crearFollow(request));
    }

    @Test
    @DisplayName("CP11 - Seguimiento a banda exitoso")
    void crearFollow_banda_exitoso() {
        User follower = new User();
        Band banda = new Band();
        Follow follow = Follow.builder().follower(follower).followedBand(banda).fechaSeguimiento(LocalDateTime.now()).build();

        FollowRequest request = new FollowRequest(1L, null, 3L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(follower));
        when(followRepository.existsByFollowerUserIdAndFollowedBandBandId(1L, 3L)).thenReturn(false);
        when(bandRepository.findById(3L)).thenReturn(Optional.of(banda));
        when(followRepository.save(any(Follow.class))).thenReturn(follow);

        FollowResponse response = followService.crearFollow(request);
        assertNotNull(response);
    }

    @Test
    @DisplayName("CP12 - Usuario no existe")
    void listarPerfilesSeguidos_usuarioNoExiste_exception() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> followService.listarPerfilesSeguidos(1L));
    }

    @Test
    @DisplayName("CP13 - Usuario no sigue a nadie")
    void listarPerfilesSeguidos_listaVacia_exception() {
        User usuario = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(followRepository.findByFollowerUserId(1L)).thenReturn(List.of());

        assertThrows(BusinessRuleException.class, () -> followService.listarPerfilesSeguidos(1L));
    }

    @Test
    @DisplayName("CP14 - Usuario sigue a otros usuarios")
    void listarPerfilesSeguidos_soloUsuarios_ok() {
        User usuario = new User();
        User seguido1 = new User();
        seguido1.setUserId(10L);
        seguido1.setNombreArtistico("Artista 1");
        seguido1.setDisponibilidad(true);
        seguido1.setUbicacion("Lima");

        Follow follow1 = Follow.builder().follower(usuario).followedUser(seguido1).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(followRepository.findByFollowerUserId(1L)).thenReturn(List.of(follow1));

        List<FollowedProfileResponse> result = followService.listarPerfilesSeguidos(1L);

        assertEquals(1, result.size());
        assertEquals("Usuario", result.get(0).tipo());
        assertEquals("Artista 1", result.get(0).nombre());
    }

    @Test
    @DisplayName("CP15 - Usuario sigue a bandas")
    void listarPerfilesSeguidos_soloBandas_ok() {
        User usuario = new User();
        Band banda = new Band();
        banda.setBandId(20L);
        banda.setNombre("Banda Rock");

        Follow follow = Follow.builder().follower(usuario).followedBand(banda).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(followRepository.findByFollowerUserId(1L)).thenReturn(List.of(follow));

        List<FollowedProfileResponse> result = followService.listarPerfilesSeguidos(1L);

        assertEquals(1, result.size());
        assertEquals("Banda", result.get(0).tipo());
        assertEquals("Banda Rock", result.get(0).nombre());
    }

    @Test
    @DisplayName("CP16 - Usuario sigue a usuarios y bandas")
    void listarPerfilesSeguidos_usuariosYBandas_ok() {
        User usuario = new User();

        User seguido = new User();
        seguido.setUserId(10L);
        seguido.setNombreArtistico("Solista A");
        seguido.setDisponibilidad(true);
        seguido.setUbicacion("Arequipa");

        Band banda = new Band();
        banda.setBandId(20L);
        banda.setNombre("Grupo B");

        Follow f1 = Follow.builder().follower(usuario).followedUser(seguido).build();
        Follow f2 = Follow.builder().follower(usuario).followedBand(banda).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(followRepository.findByFollowerUserId(1L)).thenReturn(List.of(f1, f2));

        List<FollowedProfileResponse> result = followService.listarPerfilesSeguidos(1L);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(r -> r.tipo().equals("Usuario")));
        assertTrue(result.stream().anyMatch(r -> r.tipo().equals("Banda")));
    }

    @Test
    @DisplayName("CP17 - No se especifica ni usuario ni banda")
    void eliminarFollow_sinObjetivo_exception() {
        UnfollowRequest request = new UnfollowRequest(1L, null, null);
        assertThrows(BusinessRuleException.class, () -> followService.eliminarFollow(request));
    }

    @Test
    @DisplayName("CP18 - Se especifica usuario y banda a la vez")
    void eliminarFollow_ambosObjetivos_exception() {
        UnfollowRequest request = new UnfollowRequest(1L, 2L, 3L);
        assertThrows(BusinessRuleException.class, () -> followService.eliminarFollow(request));
    }

    @Test
    @DisplayName("CP19 - No existe follow al usuario")
    void eliminarFollow_usuarioNoSeguido_exception() {
        UnfollowRequest request = new UnfollowRequest(1L, 2L, null);
        when(followRepository.findByFollowerUserIdAndFollowedUserUserId(1L, 2L)).thenReturn(Optional.empty());

        assertThrows(BusinessRuleException.class, () -> followService.eliminarFollow(request));
    }

    @Test
    @DisplayName("CP20 - Eliminación de follow a usuario exitosa")
    void eliminarFollow_usuario_ok() {
        User seguido = new User();
        seguido.setNombreArtistico("Artista 1");

        Follow follow = Follow.builder().followedUser(seguido).build();
        UnfollowRequest request = new UnfollowRequest(1L, 2L, null);

        when(followRepository.findByFollowerUserIdAndFollowedUserUserId(1L, 2L)).thenReturn(Optional.of(follow));

        Map<String, String> result = followService.eliminarFollow(request);

        verify(followRepository, times(1)).delete(follow);
        assertEquals("Has dejado de seguir a Artista 1.", result.get("message"));
    }

    @Test
    @DisplayName("CP21 - No existe follow a la banda")
    void eliminarFollow_bandaNoSeguida_exception() {
        UnfollowRequest request = new UnfollowRequest(1L, null, 3L);
        when(followRepository.findByFollowerUserIdAndFollowedBandBandId(1L, 3L)).thenReturn(Optional.empty());

        assertThrows(BusinessRuleException.class, () -> followService.eliminarFollow(request));
    }

    @Test
    @DisplayName("CP22 - Eliminación de follow a banda exitosa")
    void eliminarFollow_banda_ok() {
        Band banda = new Band();
        banda.setNombre("Banda X");

        Follow follow = Follow.builder().followedBand(banda).build();
        UnfollowRequest request = new UnfollowRequest(1L, null, 3L);

        when(followRepository.findByFollowerUserIdAndFollowedBandBandId(1L, 3L)).thenReturn(Optional.of(follow));

        Map<String, String> result = followService.eliminarFollow(request);

        verify(followRepository, times(1)).delete(follow);
        assertEquals("Has dejado de seguir a Banda X.", result.get("message"));
    }
}
