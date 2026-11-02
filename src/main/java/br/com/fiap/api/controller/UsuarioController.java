package br.com.fiap.api.controller;

import br.com.fiap.api.bo.UsuarioBo;
import br.com.fiap.api.config.Autenticacao;
import br.com.fiap.api.dao.UsuarioDao;
import br.com.fiap.api.dto.*;
import br.com.fiap.api.exception.EntidadeNaoEncontradaException;
import br.com.fiap.api.model.Usuario;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {
    private final UsuarioDao dao;
    private final UsuarioBo bo;
    private final ModelMapper mapper;
    private final Autenticacao autenticacao;

    public UsuarioController(UsuarioDao dao, UsuarioBo bo, ModelMapper mapper, Autenticacao autenticacao) {
        this.dao = dao;
        this.bo = bo;
        this.mapper = mapper;
        this.autenticacao = autenticacao;
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDto> cadastrar(@Valid @RequestBody LoginRequestDto dto,
                                                         UriComponentsBuilder builder) throws SQLException {
        Usuario usuario = bo.cadastrar(dto);
        return ResponseEntity.created(builder.path("/usuarios/{id}").buildAndExpand(usuario.getId()).toUri())
                .body(mapper.map(usuario, UsuarioResponseDto.class));
    }

    @PostMapping("/login")
    public UsuarioResponseDto login(@Valid @RequestBody LoginRequestDto dto, HttpServletRequest request)
            throws SQLException {
        Usuario usuario = bo.login(dto);
        HttpSession anterior = request.getSession(false);
        if (anterior != null) anterior.invalidate();
        autenticacao.iniciar(request.getSession(true), usuario);
        return mapper.map(usuario, UsuarioResponseDto.class);
    }

    @GetMapping
    public List<UsuarioResponseDto> listar(HttpSession session) throws SQLException {
        autenticacao.administrador(session);
        return dao.listar().stream().map(usuario -> mapper.map(usuario, UsuarioResponseDto.class)).toList();
    }

    @GetMapping("/{id}")
    public UsuarioResponseDto buscar(@PathVariable int id, HttpSession session)
            throws SQLException, EntidadeNaoEncontradaException {
        autenticacao.proprioOuAdministrador(session, id);
        return mapper.map(dao.buscar(id), UsuarioResponseDto.class);
    }

    @GetMapping("/me")
    public UsuarioResponseDto atual(HttpSession session) throws SQLException {
        return mapper.map(autenticacao.usuario(session), UsuarioResponseDto.class);
    }

    @PutMapping("/{id}")
    public UsuarioResponseDto atualizar(@PathVariable int id, @Valid @RequestBody UsuarioAtualizacaoRequestDto dto,
                                        HttpSession session) throws SQLException, EntidadeNaoEncontradaException {
        Usuario solicitante = autenticacao.proprioOuAdministrador(session, id);
        Usuario usuario = bo.atualizar(id, dto);
        if (solicitante.getId() == id) autenticacao.iniciar(session, usuario);
        return mapper.map(usuario, UsuarioResponseDto.class);
    }

    @PutMapping("/me")
    public UsuarioResponseDto atualizarAtual(@Valid @RequestBody UsuarioAtualizacaoRequestDto dto,
                                             HttpSession session) throws SQLException, EntidadeNaoEncontradaException {
        return atualizar(autenticacao.usuario(session).getId(), dto, session);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable int id, HttpSession session)
            throws SQLException, EntidadeNaoEncontradaException {
        Usuario solicitante = autenticacao.proprioOuAdministrador(session, id);
        dao.remover(id);
        if (solicitante.getId() == id) session.invalidate();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> removerAtual(HttpSession session) throws SQLException, EntidadeNaoEncontradaException {
        return remover(autenticacao.usuario(session).getId(), session);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }
}
