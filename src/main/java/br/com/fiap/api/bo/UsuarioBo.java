package br.com.fiap.api.bo;

import br.com.fiap.api.config.Senha;
import br.com.fiap.api.dao.UsuarioDao;
import br.com.fiap.api.dto.LoginRequestDto;
import br.com.fiap.api.dto.UsuarioCadastroRequestDto;
import br.com.fiap.api.dto.UsuarioAtualizacaoRequestDto;
import br.com.fiap.api.exception.EntidadeNaoEncontradaException;
import br.com.fiap.api.model.Usuario;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.sql.SQLException;
import java.util.Locale;

@Component
public class UsuarioBo {
    private final UsuarioDao dao;

    public UsuarioBo(UsuarioDao dao) {
        this.dao = dao;
    }

    public Usuario cadastrar(UsuarioCadastroRequestDto dto) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setNomeUsuario(dto.getNomeUsuario().toLowerCase(Locale.ROOT));
        usuario.setSenha(Senha.gerar(dto.getSenha()));
        usuario.setPerfil(dto.getPerfil());
        dao.cadastrar(usuario);
        return usuario;
    }

    public Usuario login(LoginRequestDto dto) throws SQLException {
        Usuario usuario = dao.buscarPorNome(dto.getNomeUsuario().toLowerCase(Locale.ROOT));
        if (usuario == null || !Senha.conferir(dto.getSenha(), usuario.getSenha()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos");
        return usuario;
    }

    public Usuario atualizar(int id, UsuarioAtualizacaoRequestDto dto)
            throws SQLException, EntidadeNaoEncontradaException {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNomeUsuario(dto.getNomeUsuario().toLowerCase(Locale.ROOT));
        if (dto.getSenha() != null) usuario.setSenha(Senha.gerar(dto.getSenha()));
        dao.atualizar(usuario);
        return dao.buscar(id);
    }
}
