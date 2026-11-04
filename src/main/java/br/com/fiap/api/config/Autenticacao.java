package br.com.fiap.api.config;

import br.com.fiap.api.dao.UsuarioDao;
import br.com.fiap.api.exception.EntidadeNaoEncontradaException;
import br.com.fiap.api.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.sql.SQLException;

@Component
public class Autenticacao {
    private final UsuarioDao dao;

    public Autenticacao(UsuarioDao dao) {
        this.dao = dao;
    }

    public void iniciar(HttpSession session, Usuario usuario) {
        session.setAttribute("usuarioId", usuario.getId());
        session.setAttribute("credencial", usuario.getSenha());
    }

    public Usuario usuario(HttpSession session) throws SQLException {
        Integer id = (Integer) session.getAttribute("usuarioId");
        if (id == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login");
        try {
            // Reconsulta o banco: conta excluída, senha trocada e perfil alterado não ficam em cache.
            Usuario usuario = dao.buscar(id);
            if (!usuario.getSenha().equals(session.getAttribute("credencial"))) {
                session.invalidate();
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login novamente");
            }
            return usuario;
        } catch (EntidadeNaoEncontradaException e) {
            session.invalidate();
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Faça login novamente");
        }
    }

    public Usuario administrador(HttpSession session) throws SQLException {
        Usuario usuario = usuario(session);
        if (usuario.getPerfil() != 1)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso exclusivo do administrador");
        return usuario;
    }

    public Usuario proprioOuAdministrador(HttpSession session, int id) throws SQLException {
        Usuario usuario = usuario(session);
        if (usuario.getId() != id && usuario.getPerfil() != 1)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Acesso restrito à própria conta");
        return usuario;
    }
}
