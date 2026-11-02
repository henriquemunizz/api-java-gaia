package br.com.fiap.api.controller;

import br.com.fiap.api.config.Autenticacao;
import br.com.fiap.api.bo.UsuarioProgressoBo;
import br.com.fiap.api.dao.*;
import br.com.fiap.api.dto.*;
import br.com.fiap.api.exception.EntidadeNaoEncontradaException;
import jakarta.servlet.http.HttpSession;
import org.modelmapper.ModelMapper;
import org.springframework.web.bind.annotation.*;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/usuarios/me")
public class UsuarioProgressoController {
    private final UsuarioProgressoDao dao;
    private final UsuarioProgressoBo bo;
    private final UsuarioMissaoDao missaoDao;
    private final UsuarioRecompensaDao recompensaDao;
    private final Autenticacao autenticacao;
    private final ModelMapper mapper;

    public UsuarioProgressoController(UsuarioProgressoDao dao, UsuarioProgressoBo bo,
                                     UsuarioMissaoDao missaoDao, UsuarioRecompensaDao recompensaDao,
                                     Autenticacao autenticacao, ModelMapper mapper) {
        this.dao = dao;
        this.bo = bo;
        this.missaoDao = missaoDao;
        this.recompensaDao = recompensaDao;
        this.autenticacao = autenticacao;
        this.mapper = mapper;
    }

    @GetMapping("/progresso")
    public ProgressoResponseDto buscar(HttpSession session) throws SQLException, EntidadeNaoEncontradaException {
        return mapper.map(dao.buscar(autenticacao.usuario(session).getId()), ProgressoResponseDto.class);
    }

    @PostMapping("/missoes/{id}/concluir")
    public ProgressoResponseDto concluir(@PathVariable int id, HttpSession session)
            throws SQLException, EntidadeNaoEncontradaException {
        return mapper.map(bo.concluir(autenticacao.usuario(session).getId(), id), ProgressoResponseDto.class);
    }

    @PostMapping("/recompensas/{id}/resgatar")
    public ProgressoResponseDto resgatar(@PathVariable int id, HttpSession session)
            throws SQLException, EntidadeNaoEncontradaException {
        return mapper.map(bo.resgatar(autenticacao.usuario(session).getId(), id), ProgressoResponseDto.class);
    }

    @GetMapping("/missoes")
    public List<HistoricoResponseDto> missoes(HttpSession session) throws SQLException {
        return missaoDao.listarPorUsuario(autenticacao.usuario(session).getId()).stream().map(registro -> {
            HistoricoResponseDto dto = new HistoricoResponseDto();
            dto.setId(registro.getId());
            dto.setItemId(registro.getMissao().getId());
            dto.setPontos(registro.getPontos());
            dto.setData(registro.getData().toString());
            return dto;
        }).toList();
    }

    @GetMapping("/recompensas")
    public List<HistoricoResponseDto> recompensas(HttpSession session) throws SQLException {
        return recompensaDao.listarPorUsuario(autenticacao.usuario(session).getId()).stream().map(registro -> {
            HistoricoResponseDto dto = new HistoricoResponseDto();
            dto.setId(registro.getId());
            dto.setItemId(registro.getRecompensa().getId());
            dto.setPontos(registro.getPontos());
            dto.setData(registro.getData().toString());
            return dto;
        }).toList();
    }
}
