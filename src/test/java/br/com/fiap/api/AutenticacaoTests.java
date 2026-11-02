package br.com.fiap.api;

import br.com.fiap.api.config.*;
import br.com.fiap.api.dao.UsuarioDao;
import br.com.fiap.api.exception.EntidadeNaoEncontradaException;
import br.com.fiap.api.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutenticacaoTests {
    @Test void senhaProtegidaConfereSemGuardarTextoPuro() {
        String hash = Senha.gerar("senhaDeTeste123");
        assertNotEquals("senhaDeTeste123", hash);
        assertTrue(Senha.conferir("senhaDeTeste123", hash));
        assertFalse(Senha.conferir("senhaErrada", hash));
        assertNotEquals(hash, Senha.gerar("senhaDeTeste123"));
    }

    @Test void acessoExigeLoginEAdministrador() throws Exception {
        UsuarioDao dao = mock(UsuarioDao.class, withSettings().mockMaker("mock-maker-subclass"));
        Autenticacao auth = new Autenticacao(dao);
        MockHttpSession session = new MockHttpSession();
        assertEquals(401, assertThrows(ResponseStatusException.class, () -> auth.usuario(session)).getStatusCode().value());
        Usuario usuario = usuario();
        when(dao.buscar(1)).thenReturn(usuario);
        auth.iniciar(session, usuario);
        assertEquals(403, assertThrows(ResponseStatusException.class, () -> auth.administrador(session)).getStatusCode().value());
        assertEquals(403, assertThrows(ResponseStatusException.class, () -> auth.proprioOuAdministrador(session, 2)).getStatusCode().value());
        assertEquals(usuario, auth.proprioOuAdministrador(session, 1));
        usuario.setPerfil("ADMIN");
        assertEquals(usuario, auth.administrador(session));
        assertEquals(usuario, auth.proprioOuAdministrador(session, 2));
    }

    @Test void contaExcluidaPerdeAcessoNaSessaoExistente() throws Exception {
        UsuarioDao dao = mock(UsuarioDao.class, withSettings().mockMaker("mock-maker-subclass"));
        Autenticacao auth = new Autenticacao(dao);
        MockHttpSession session = new MockHttpSession();
        auth.iniciar(session, usuario());
        when(dao.buscar(1)).thenThrow(new EntidadeNaoEncontradaException("Usuário não encontrado"));
        assertEquals(401, assertThrows(ResponseStatusException.class, () -> auth.usuario(session)).getStatusCode().value());
        assertTrue(session.isInvalid());
    }

    @Test void senhaAlteradaInvalidaOutraSessao() throws Exception {
        UsuarioDao dao = mock(UsuarioDao.class, withSettings().mockMaker("mock-maker-subclass"));
        Autenticacao auth = new Autenticacao(dao);
        MockHttpSession session = new MockHttpSession();
        Usuario usuario = usuario();
        auth.iniciar(session, usuario);
        usuario.setSenha("novo-hash");
        when(dao.buscar(1)).thenReturn(usuario);
        assertEquals(401, assertThrows(ResponseStatusException.class, () -> auth.usuario(session)).getStatusCode().value());
        assertTrue(session.isInvalid());
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setPerfil("USUARIO");
        usuario.setSenha("hash");
        return usuario;
    }
}
