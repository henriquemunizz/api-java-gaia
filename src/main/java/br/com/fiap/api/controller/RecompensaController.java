package br.com.fiap.api.controller;

import br.com.fiap.api.dao.RecompensaDao;
import br.com.fiap.api.dto.*;
import br.com.fiap.api.model.Recompensa;
import br.com.fiap.api.exception.EntidadeNaoEncontradaException;
import br.com.fiap.api.config.Autenticacao;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/recompensas")
public class RecompensaController {
    private final RecompensaDao dao;
    private final ModelMapper mapper;
    private final Autenticacao autenticacao;
    public RecompensaController(RecompensaDao dao, ModelMapper mapper, Autenticacao autenticacao) { this.dao=dao; this.mapper=mapper; this.autenticacao=autenticacao; }
    @GetMapping
    public List<RecompensaResponseDto> listar(HttpSession session) throws SQLException {
        autenticacao.usuario(session);
        return dao.listar().stream().map(item -> mapper.map(item, RecompensaResponseDto.class)).toList();
    }
    @GetMapping("/{id}")
    public RecompensaResponseDto buscar(@PathVariable int id, HttpSession session) throws SQLException, EntidadeNaoEncontradaException {
        autenticacao.usuario(session);
        return mapper.map(dao.buscar(id), RecompensaResponseDto.class);
    }
    @PostMapping
    public ResponseEntity<RecompensaResponseDto> cadastrar(@Valid @RequestBody RecompensaRequestDto dto, HttpSession session, UriComponentsBuilder builder) throws SQLException {
        autenticacao.administrador(session);
        Recompensa item=mapper.map(dto,Recompensa.class);
        dao.cadastrar(item);
        return ResponseEntity.created(builder.path("/recompensas/{id}").buildAndExpand(item.getId()).toUri()).body(mapper.map(item,RecompensaResponseDto.class));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(@PathVariable int id, @Valid @RequestBody RecompensaRequestDto dto, HttpSession session) throws SQLException, EntidadeNaoEncontradaException {
        autenticacao.administrador(session);
        Recompensa item=mapper.map(dto,Recompensa.class); item.setId(id); dao.atualizar(item);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable int id, HttpSession session) throws SQLException, EntidadeNaoEncontradaException {
        autenticacao.administrador(session); dao.remover(id);
        return ResponseEntity.noContent().build();
    }
}
