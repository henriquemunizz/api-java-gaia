package br.com.fiap.api.controller;

import br.com.fiap.api.bo.ChatBo;
import br.com.fiap.api.config.Autenticacao;
import br.com.fiap.api.dao.ConversaDao;
import br.com.fiap.api.dto.ChatMensagemRequestDto;
import br.com.fiap.api.exception.EntidadeNaoEncontradaException;
import br.com.fiap.api.model.Conversa;
import br.com.fiap.api.model.Mensagem;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/usuarios/me/conversas")
public class ChatController {
    private final ConversaDao dao;
    private final ChatBo bo;
    private final Autenticacao autenticacao;
    public ChatController(ConversaDao dao, ChatBo bo, Autenticacao autenticacao) {
        this.dao = dao;
        this.bo = bo;
        this.autenticacao = autenticacao;
    }

    @GetMapping
    public List<Conversa> listar(HttpSession session) throws SQLException {
        return dao.listar(autenticacao.usuario(session).getId());
    }

    @PostMapping
    public ResponseEntity<Conversa> criar(HttpSession session) throws SQLException {
        Conversa conversa = dao.criar(autenticacao.usuario(session).getId());
        return ResponseEntity.created(URI.create("/usuarios/me/conversas/" + conversa.getId() + "/mensagens")).body(conversa);
    }

    @GetMapping("/{id}/mensagens")
    public List<Mensagem> mensagens(@PathVariable int id, HttpSession session) throws SQLException {
        return dao.mensagens(id, autenticacao.usuario(session).getId());
    }

    @PostMapping("/{id}/mensagens")
    public List<Mensagem> enviar(@PathVariable int id, @Valid @RequestBody ChatMensagemRequestDto dto, HttpSession session)
            throws SQLException, EntidadeNaoEncontradaException {
        return bo.enviar(id, autenticacao.usuario(session), dto);
    }
}
