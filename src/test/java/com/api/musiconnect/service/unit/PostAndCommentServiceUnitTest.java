package com.api.musiconnect.service.unit;

import com.api.musiconnect.dto.request.ComentarioRequest;
import com.api.musiconnect.dto.request.PostRequest;
import com.api.musiconnect.dto.request.PostUpdateRequest;
import com.api.musiconnect.dto.response.ComentarioResponse;
import com.api.musiconnect.dto.response.PostResponse;
import com.api.musiconnect.exception.BusinessRuleException;
import com.api.musiconnect.exception.ResourceNotFoundException;
import com.api.musiconnect.exception.BadRequestException;
import com.api.musiconnect.model.entity.Comentario;
import com.api.musiconnect.model.entity.Post;
import com.api.musiconnect.model.entity.User;
import com.api.musiconnect.model.enums.PostTipo;
import com.api.musiconnect.repository.ComentarioRepository;
import com.api.musiconnect.repository.PostRepository;
import com.api.musiconnect.repository.UserRepository;
import com.api.musiconnect.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostAndCommentServiceUnitTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ComentarioRepository comentarioRepository;

    @InjectMocks
    private PostService postService;

    private User usuarioAutenticado;
    private User otroUsuario;
    private Post post;

    @BeforeEach
    void setUp() {
        usuarioAutenticado = User.builder()
                .userId(1L)
                .nombreArtistico("Usuario Test")
                .build();

        otroUsuario = User.builder()
                .userId(2L)
                .nombreArtistico("Otro Usuario")
                .build();

        post = Post.builder()
                .postId(1L)
                .usuario(usuarioAutenticado)
                .contenido("Contenido de prueba")
                .tipo(PostTipo.TEXTO)
                .fechaPublicacion(LocalDateTime.now())
                .comentarios(new ArrayList<>())
                .build();
    }

    // Pruebas para Crear Publicación
    @Test
    @DisplayName("CP01: Crear publicación exitosamente")
    void crearPost_ConDatosValidos_DeberiaCrearPost() {
        PostRequest request = new PostRequest(1L, "Contenido válido", "TEXTO");
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioAutenticado));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        PostResponse response = postService.crearPost(request);

        assertNotNull(response);
        assertEquals("Publicación creada exitosamente.", response.message());
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("CP02: Crear publicación con contenido vacío")
    void crearPost_ContenidoVacio_DeberiaLanzarExcepcion() {
        PostRequest request = new PostRequest(1L, "", "TEXTO");

        assertThrows(BusinessRuleException.class, () -> postService.crearPost(request));
    }

    @Test
    @DisplayName("CP03: Crear publicación con contenido que excede límite")
    void crearPost_ContenidoExcedeLimite_DeberiaLanzarExcepcion() {
        PostRequest request = new PostRequest(1L, "a".repeat(501), "TEXTO");

        assertThrows(BusinessRuleException.class, () -> postService.crearPost(request));
    }

    @Test
    @DisplayName("CP04: Crear publicación con usuario inexistente")
    void crearPost_UsuarioInexistente_DeberiaLanzarExcepcion() {
        PostRequest request = new PostRequest(999L, "Contenido válido", "TEXTO");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> postService.crearPost(request));
    }

    @Test
    @DisplayName("CP05: Crear publicación con tipo inválido")
    void crearPost_TipoInvalido_DeberiaLanzarExcepcion() {
        PostRequest request = new PostRequest(1L, "Contenido válido", "TIPO_INVALIDO");
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioAutenticado));

        assertThrows(IllegalArgumentException.class, () -> postService.crearPost(request));
    }

    @Test
    @DisplayName("CP06: Editar publicación exitosamente")
    void editarPost_DatosValidos_DeberiaActualizarPost() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        PostUpdateRequest request = new PostUpdateRequest(1L, "Nuevo contenido");

        PostResponse response = postService.editarPost(1L, request);

        assertEquals("Publicación actualizada correctamente.", response.message());
        assertEquals("Nuevo contenido", post.getContenido());
    }

    @Test
    @DisplayName("CP07: Editar publicación inexistente")
    void editarPost_PostInexistente_DeberiaLanzarExcepcion() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());
        PostUpdateRequest request = new PostUpdateRequest(1L, "Nuevo contenido");

        assertThrows(ResourceNotFoundException.class, () -> postService.editarPost(1L, request));
    }

    @Test
    @DisplayName("CP08: Editar publicación sin permisos")
    void editarPost_SinPermisos_DeberiaLanzarExcepcion() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        PostUpdateRequest request = new PostUpdateRequest(2L, "Nuevo contenido");

        assertThrows(BusinessRuleException.class, () -> postService.editarPost(1L, request));
    }

    @Test
    @DisplayName("CP09: Crear comentario exitosamente")
    void crearComentario_DatosValidos_DeberiaCrearComentario() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        ComentarioRequest request = new ComentarioRequest(1L, "Comentario válido");

        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioAutenticado));
        Comentario comentarioCreado = Comentario.builder()
                .contenido("Comentario válido")
                .usuario(usuarioAutenticado)
                .post(post)
                .fechaComentario(LocalDateTime.now())
                .build();
        when(comentarioRepository.save(any(Comentario.class))).thenReturn(comentarioCreado);

        ComentarioResponse response = postService.comentarPost(1L, request);

        assertNotNull(response);
        assertEquals(comentarioCreado.getContenido(), response.contenido());
        verify(comentarioRepository).save(any(Comentario.class));
    }



    @Test
    @DisplayName("CP10: Crear comentario en post inexistente")
    void crearComentario_PostInexistente_DeberiaLanzarExcepcion() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());
        ComentarioRequest request = new ComentarioRequest(1L, "Comentario válido");

        assertThrows(ResourceNotFoundException.class, () -> postService.comentarPost(1L, request));
    }

    @Test
    @DisplayName("CP11: Crear comentario con contenido vacío")
    void crearComentario_ContenidoVacio_DeberiaLanzarExcepcion() {
        ComentarioRequest request = new ComentarioRequest(1L, "");
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioAutenticado));

        assertThrows(BusinessRuleException.class, () -> postService.comentarPost(1L, request));
    }

    @Test
    @DisplayName("CP12: Crear comentario que excede límite")
    void crearComentario_ExcedeLimite_DeberiaLanzarExcepcion() {
        ComentarioRequest request = new ComentarioRequest(1L, "a".repeat(301));
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioAutenticado));

        assertThrows(BusinessRuleException.class, () -> postService.comentarPost(1L, request));
    }

    @Test
    @DisplayName("CP13: Crear comentario con usuario inexistente")
    void crearComentario_UsuarioInexistente_DeberiaLanzarExcepcion() {
        ComentarioRequest request = new ComentarioRequest(999L, "Comentario válido");
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> postService.comentarPost(1L, request));
    }

    @Test
    @DisplayName("CP14: Listar comentarios de un post")
    void listarComentarios_PostExistente_DeberiaRetornarLista() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        List<Comentario> comentarios = new ArrayList<>();
        comentarios.add(Comentario.builder()
                .contenido("Comentario 1")
                .usuario(usuarioAutenticado)
                .post(post)
                .fechaComentario(LocalDateTime.now())
                .build());
        when(comentarioRepository.findByPost(post)).thenReturn(comentarios);

        List<ComentarioResponse> response = postService.listarComentarios(1L);

        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(1, response.size());
    }

    @Test
    @DisplayName("CP15: Listar comentarios de post inexistente")
    void listarComentarios_PostInexistente_DeberiaLanzarExcepcion() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> postService.listarComentarios(1L));
    }

    @Test
    @DisplayName("CP16: Verificar formato de fecha en respuesta")
    void verificarFormatoFecha_EnRespuesta_DeberiaSerValido() {
        PostRequest request = new PostRequest(1L, "Contenido válido", "TEXTO");
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioAutenticado));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        PostResponse response = postService.crearPost(request);

        assertNotNull(response.fechaPublicacion());
        assertTrue(response.fechaPublicacion() instanceof LocalDateTime);
    }

    @Test
    @DisplayName("CP17: Verificar persistencia de tipo de post")
    void verificarTipoPost_AlCrear_DeberiaPersistirCorrectamente() {
        PostRequest request = new PostRequest(1L, "Contenido válido", "TEXTO");
        when(userRepository.findById(1L)).thenReturn(Optional.of(usuarioAutenticado));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        PostResponse response = postService.crearPost(request);

        assertEquals("TEXTO", response.tipo());
        verify(postRepository).save(any(Post.class));
    }
}