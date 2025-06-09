package com.api.musiconnect.service.unit;

import com.api.musiconnect.dto.request.AddMemberRequest;
import com.api.musiconnect.dto.request.BandRequest;
import com.api.musiconnect.dto.request.BandUpdateRequest;
import com.api.musiconnect.dto.response.BandResponse;
import com.api.musiconnect.exception.BusinessRuleException;
import com.api.musiconnect.exception.ResourceNotFoundException;
import com.api.musiconnect.model.entity.Band;
import com.api.musiconnect.model.entity.MusicGenre;
import com.api.musiconnect.model.entity.User;
import com.api.musiconnect.model.enums.MusicGenreEnum;
import com.api.musiconnect.repository.BandRepository;
import com.api.musiconnect.repository.MusicGenreRepository;
import com.api.musiconnect.repository.UserRepository;
import com.api.musiconnect.service.BandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.lenient;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BandServiceUnitTest {

    @Mock private BandRepository bandRepository;
    @Mock private UserRepository userRepository;
    @Mock private MusicGenreRepository musicGenreRepository;

    @InjectMocks private BandService bandService;

    private User adminUser;
    private List<MusicGenre> generos;

    @BeforeEach
    void setup() {
        adminUser = new User();
        adminUser.setUserId(1L);
        adminUser.setNombreArtistico("Admin");
        adminUser.setDisponibilidad(true);

        generos = List.of(
                new MusicGenre(1L, MusicGenreEnum.ROCK),
                new MusicGenre(2L, MusicGenreEnum.JAZZ)
        );
    }

    @Test
    void crearBanda_deberiaCrearBandaExitosamente() {
        BandRequest request = new BandRequest("Mi Banda", "Descripción", List.of("ROCK", "JAZZ"), 1L);

        when(bandRepository.existsByNombreIgnoreCase("Mi Banda")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(musicGenreRepository.findAllByNombreIn(List.of(MusicGenreEnum.ROCK, MusicGenreEnum.JAZZ))).thenReturn(generos);

        when(bandRepository.save(any(Band.class))).thenAnswer(invocation -> {
            Band banda = invocation.getArgument(0);
            banda.setBandId(100L); // simula que fue persistida
            banda.setGenerosMusicales(generos); // importante: es List
            return banda;
        });

        BandResponse response = bandService.crearBanda(request);

        assertEquals("Mi Banda", response.nombre());
        assertEquals("Descripción", response.descripcion());
        assertEquals("Admin", response.administradorNombreArtistico());
    }


    @Test
    void crearBanda_deberiaFallar_siNombreExiste() {
        BandRequest request = new BandRequest("Duplicada", "desc", List.of("ROCK"), 1L);
        when(bandRepository.existsByNombreIgnoreCase("Duplicada")).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> bandService.crearBanda(request));
    }

    @Test
    void crearBanda_deberiaFallar_siAdminNoDisponible() {
        adminUser.setDisponibilidad(false);
        when(bandRepository.existsByNombreIgnoreCase("Mi Banda")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        BandRequest request = new BandRequest("Mi Banda", "desc", List.of("ROCK"), 1L);
        assertThrows(BusinessRuleException.class, () -> bandService.crearBanda(request));
    }

    @Test
    void crearBanda_deberiaFallar_siGeneroInvalido() {
        BandRequest request = new BandRequest("Mi Banda", "desc", List.of("NO_EXISTE"), 1L);
        when(bandRepository.existsByNombreIgnoreCase("Mi Banda")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        assertThrows(BusinessRuleException.class, () -> bandService.crearBanda(request));
    }

    @Test
    void crearBanda_deberiaFallar_siGeneroNoEnBD() {
        BandRequest request = new BandRequest("Mi Banda", "desc", List.of("ROCK", "JAZZ"), 1L);
        when(bandRepository.existsByNombreIgnoreCase("Mi Banda")).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(musicGenreRepository.findAllByNombreIn(any())).thenReturn(List.of(new MusicGenre(1L, MusicGenreEnum.ROCK)));

        assertThrows(BusinessRuleException.class, () -> bandService.crearBanda(request));
    }

    @Test
    void addIntegrante_deberiaAñadirExitosamente() {
        // Nuevo integrante
        User nuevoIntegrante = new User();
        nuevoIntegrante.setUserId(2L);
        nuevoIntegrante.setDisponibilidad(true);

        // Banda con adminUser
        Band banda = new Band();
        banda.setBandId(1L);
        banda.setAdministrador(adminUser);
        banda.setMiembros(new ArrayList<>());

        // Request con adminId y userId
        AddMemberRequest request = new AddMemberRequest(2L, 1L);

        // Mocks
        when(bandRepository.findById(1L)).thenReturn(Optional.of(banda));
        when(userRepository.findById(2L)).thenReturn(Optional.of(nuevoIntegrante));

        // Imprimir para debug
        System.out.println("Admin User ID en test: " + adminUser.getUserId());
        System.out.println("Request adminId: " + request.adminId());

        // Ejecutar método
        Map<String, String> response = bandService.addIntegrante(1L, request);

        // Verificar
        assertTrue(banda.getMiembros().contains(nuevoIntegrante));
        assertEquals("Integrante añadido correctamente", response.get("message"));
    }


    @Test
    void addIntegrante_deberiaFallar_siNoEsAdmin() {
        User fakeAdmin = new User();
        fakeAdmin.setUserId(999L); // ID diferente al del request

        Band banda = new Band();
        banda.setAdministrador(fakeAdmin);

        when(bandRepository.findById(1L)).thenReturn(Optional.of(banda));

        AddMemberRequest request = new AddMemberRequest(1L, 2L); // adminId real: 1L

        assertThrows(BusinessRuleException.class, () -> bandService.addIntegrante(1L, request));
    }


    @Test
    void addIntegrante_deberiaFallar_siUsuarioNoDisponible() {
        Band banda = new Band();
        banda.setAdministrador(adminUser);
        banda.setMiembros(new ArrayList<>());

        User user = new User();
        user.setUserId(2L);
        user.setDisponibilidad(false);

        when(bandRepository.findById(1L)).thenReturn(Optional.of(banda));
        lenient().when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        AddMemberRequest request = new AddMemberRequest(1L, 2L);
        assertThrows(BusinessRuleException.class, () -> bandService.addIntegrante(1L, request));
    }


    @Test
    void updateBand_deberiaActualizarBandaExitosamente() {
        Band banda = new Band();
        banda.setBandId(1L);
        banda.setNombre("AntiguoNombre");
        banda.setDescripcion("desc");
        banda.setAdministrador(adminUser);

        BandUpdateRequest request = new BandUpdateRequest("NuevoNombre", "Nueva descripción", List.of("ROCK", "JAZZ"), 1L);

        when(bandRepository.findById(1L)).thenReturn(Optional.of(banda));
        when(bandRepository.existsByNombreIgnoreCase("NuevoNombre")).thenReturn(false);
        when(musicGenreRepository.findAllByNombreIn(List.of(MusicGenreEnum.ROCK, MusicGenreEnum.JAZZ))).thenReturn(generos);

        bandService.updateBand(1L, request);

        assertEquals("NuevoNombre", banda.getNombre());
        assertEquals("Nueva descripción", banda.getDescripcion());
        verify(bandRepository).save(banda);
    }

    @Test
    void updateBand_deberiaFallar_siBandaNoExiste() {
        when(bandRepository.findById(1L)).thenReturn(Optional.empty());

        BandUpdateRequest request = new BandUpdateRequest("nombre", "desc", List.of("ROCK"), 1L);

        assertThrows(ResourceNotFoundException.class, () -> bandService.updateBand(1L, request));
    }

    @Test
    void updateBand_deberiaFallar_siNombreYaExiste() {
        Band banda = new Band();
        banda.setBandId(1L);
        banda.setNombre("AntiguoNombre");
        banda.setAdministrador(adminUser);

        BandUpdateRequest request = new BandUpdateRequest("NuevoNombre", "desc", List.of("ROCK"), 1L);

        when(bandRepository.findById(1L)).thenReturn(Optional.of(banda));
        when(bandRepository.existsByNombreIgnoreCase("NuevoNombre")).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> bandService.updateBand(1L, request));
    }

    @Test
    void updateBand_deberiaFallar_siGeneroInvalido() {
        Band banda = new Band();
        banda.setBandId(1L);
        banda.setAdministrador(adminUser);
        banda.setNombre("Nombre"); // 👈 AÑADIR ESTO PARA EVITAR EL NPE

        BandUpdateRequest request = new BandUpdateRequest("Nombre", "desc", List.of("INVALIDO"), 1L);

        when(bandRepository.findById(1L)).thenReturn(Optional.of(banda));

        assertThrows(BusinessRuleException.class, () -> bandService.updateBand(1L, request));
    }


    @Test
    void updateBand_deberiaFallar_siFaltanGenerosEnBD() {
        Band banda = new Band();
        banda.setBandId(1L);
        banda.setAdministrador(adminUser);
        banda.setNombre("NombreActual"); // ✅ Agregá esta línea

        BandUpdateRequest request = new BandUpdateRequest("Nombre", "desc", List.of("ROCK", "JAZZ"), 1L);

        when(bandRepository.findById(1L)).thenReturn(Optional.of(banda));
        when(musicGenreRepository.findAllByNombreIn(any())).thenReturn(List.of(new MusicGenre(1L, MusicGenreEnum.ROCK)));

        assertThrows(BusinessRuleException.class, () -> bandService.updateBand(1L, request));
    }

}