package br.com.fiap.api.controller;

import br.com.fiap.api.dao.MissaoDao;
import br.com.fiap.api.dto.*;
import br.com.fiap.api.model.Missao;
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
@RequestMapping("/missoes")
public class MissaoController {
    private final MissaoDao dao;
    private final ModelMapper mapper;
    private final Autenticacao autenticacao;
    public MissaoController(MissaoDao dao, ModelMapper mapper, Autenticacao autenticacao) { this.dao=dao; this.mapper=mapper; this.autenticacao=autenticacao; }
    @GetMapping
    public List<MissaoResponseDto> listar(HttpSession session) throws SQLException {
        autenticacao.usuario(session);
        return dao.listar().stream().map(item -> mapper.map(item, MissaoResponseDto.class)).toList();
    }
    @GetMapping("/{id}")
    public MissaoResponseDto buscar(@PathVariable int id, HttpSession session) throws SQLException, EntidadeNaoEncontradaException {
        autenticacao.usuario(session);
        return mapper.map(dao.buscar(id), MissaoResponseDto.class);
    }
    @PostMapping
    public ResponseEntity<MissaoResponseDto> cadastrar(@Valid @RequestBody MissaoRequestDto dto, HttpSession session, UriComponentsBuilder builder) throws SQLException {
        autenticacao.administrador(session);
        Missao item=mapper.map(dto,Missao.class);
        dao.cadastrar(item);
        return ResponseEntity.created(builder.path("/missoes/{id}").buildAndExpand(item.getId()).toUri()).body(mapper.map(item,MissaoResponseDto.class));
    }
    @PutMapping("/{id}")
    public ResponseEntity<Void> atualizar(@PathVariable int id, @Valid @RequestBody MissaoRequestDto dto, HttpSession session) throws SQLException, EntidadeNaoEncontradaException {
        autenticacao.administrador(session);
        Missao item=mapper.map(dto,Missao.class); item.setId(id); dao.atualizar(item);
        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable int id, HttpSession session) throws SQLException, EntidadeNaoEncontradaException {
        autenticacao.administrador(session); dao.remover(id);
        return ResponseEntity.noContent().build();
    }
}
