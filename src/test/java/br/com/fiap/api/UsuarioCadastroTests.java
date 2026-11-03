package br.com.fiap.api;

import br.com.fiap.api.bo.UsuarioBo;
import br.com.fiap.api.config.Autenticacao;
import br.com.fiap.api.config.Senha;
import br.com.fiap.api.controller.UsuarioController;
import br.com.fiap.api.dao.UsuarioDao;
import br.com.fiap.api.dto.UsuarioCadastroRequestDto;
import br.com.fiap.api.exception.ApiExceptionHandler;
import br.com.fiap.api.factory.ConnectionFactory;
import br.com.fiap.api.model.Usuario;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UsuarioCadastroTests {
    private UsuarioDao dao() {
        return mock(UsuarioDao.class, withSettings().mockMaker("mock-maker-subclass"));
    }

    @Test void cadastroPreservaPerfilEscolhidoEProtegeSenha() throws Exception {
        for (int perfil : new int[]{0, 1}) {
            UsuarioDao dao = dao();
            UsuarioCadastroRequestDto dto = new UsuarioCadastroRequestDto();
            dto.setNomeUsuario("ContaTeste");
            dto.setSenha("SenhaTeste123");
            dto.setPerfil(perfil);
            Usuario usuario = new UsuarioBo(dao).cadastrar(dto);
            assertEquals(perfil, usuario.getPerfil());
            assertEquals("contateste", usuario.getNomeUsuario());
            assertTrue(Senha.conferir(dto.getSenha(), usuario.getSenha()));
            verify(dao).cadastrar(usuario);
        }
    }

    @Test void contratoCadastroAceitaZeroEUmMasRecusaPerfilAusenteOuForaDoIntervalo() throws Exception {
        UsuarioDao dao = dao();
        UsuarioController controller = new UsuarioController(dao, new UsuarioBo(dao), new ModelMapper(), new Autenticacao(dao));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new ApiExceptionHandler()).build();

        for (int perfil : new int[]{0, 1}) {
            mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nomeUsuario\":\"contateste\",\"senha\":\"SenhaTeste123\",\"perfil\":" + perfil + "}"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.perfil").value(perfil))
                    .andExpect(jsonPath("$.senha").doesNotExist());
        }
        for (String perfil : new String[]{"null", "-1", "2", "\"ADMIN\""}) {
            mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                    .content("{\"nomeUsuario\":\"contateste\",\"senha\":\"SenhaTeste123\",\"perfil\":" + perfil + "}"))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(post("/usuarios").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nomeUsuario\":\"contateste\",\"senha\":\"SenhaTeste123\"}"))
                .andExpect(status().isBadRequest());
        verify(dao, times(2)).cadastrar(any(Usuario.class));
    }

    @Test void jdbcGravaPerfilNumericoSemForcarUsuarioComum() throws Exception {
        ConnectionFactory factory = mock(ConnectionFactory.class, withSettings().mockMaker("mock-maker-subclass"));
        Connection connection = mock(Connection.class, withSettings().mockMaker("mock-maker-subclass"));
        PreparedStatement insert = mock(PreparedStatement.class, withSettings().mockMaker("mock-maker-subclass"));
        PreparedStatement progress = mock(PreparedStatement.class, withSettings().mockMaker("mock-maker-subclass"));
        ResultSet keys = mock(ResultSet.class, withSettings().mockMaker("mock-maker-subclass"));
        when(factory.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), any(String[].class))).thenReturn(insert);
        when(connection.prepareStatement(anyString())).thenReturn(progress);
        when(insert.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(true);
        when(keys.getInt(1)).thenReturn(42);
        Usuario usuario = new Usuario();
        usuario.setNomeUsuario("admin_teste");
        usuario.setSenha("hash");
        usuario.setPerfil(1);

        new UsuarioDao(factory).cadastrar(usuario);

        verify(insert).setInt(3, 1);
        verify(progress).setInt(1, 42);
        verify(connection).commit();
        assertEquals(1, usuario.getPerfil());
        assertEquals(42, usuario.getId());
    }
}
