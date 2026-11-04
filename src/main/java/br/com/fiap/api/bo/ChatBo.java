package br.com.fiap.api.bo;

import br.com.fiap.api.config.N8nChat;
import br.com.fiap.api.dao.*;
import br.com.fiap.api.dto.*;
import br.com.fiap.api.exception.EntidadeNaoEncontradaException;
import br.com.fiap.api.model.*;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Component
public class ChatBo {
    private final ConversaDao conversas;
    private final MissaoDao missoes;
    private final RecompensaDao recompensas;
    private final UsuarioProgressoDao progressos;
    private final N8nChat n8n;
    private final ModelMapper mapper;

    public ChatBo(ConversaDao conversas, MissaoDao missoes, RecompensaDao recompensas,
                  UsuarioProgressoDao progressos, N8nChat n8n, ModelMapper mapper) {
        this.conversas = conversas;
        this.missoes = missoes;
        this.recompensas = recompensas;
        this.progressos = progressos;
        this.n8n = n8n;
        this.mapper = mapper;
    }

    public List<Mensagem> enviar(int conversa, Usuario usuario, ChatMensagemRequestDto dto)
            throws SQLException, EntidadeNaoEncontradaException {
        List<Mensagem> historico = conversas.mensagens(conversa, usuario.getId());
        String texto = dto.getTexto().trim();
        String requisicao = dto.getRequisicaoId().toLowerCase(java.util.Locale.ROOT);
        for (Mensagem m : historico) {
            if (m.getRequisicaoId().equals(requisicao)) {
                if (!m.getTexto().equals(texto))
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Identificador de envio já utilizado");
                return historico;
            }
        }
        if (historico.size() >= 400)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta conversa atingiu o limite. Comece uma nova conversa.");

        Map<String, Object> contexto = Map.of(
                "usuario", Map.of("nomeUsuario", usuario.getNomeUsuario()),
                "progresso", mapper.map(progressos.buscar(usuario.getId()), ProgressoResponseDto.class),
                "missoes", missoes.listar().stream().filter(m -> m.getAtivo() == 1).limit(100)
                        .map(m -> mapper.map(m, MissaoResponseDto.class)).toList(),
                "recompensas", recompensas.listar().stream().filter(r -> r.getAtivo() == 1).limit(100)
                        .map(r -> mapper.map(r, RecompensaResponseDto.class)).toList(),
                "historico", historico.stream().skip(Math.max(0, historico.size() - 20))
                        .map(m -> Map.of("autor", m.getAutor(), "texto", m.getTexto())).toList(),
                "mensagem", texto);
        String resposta = n8n.responder(contexto);
        int ultima = historico.isEmpty() ? 0 : historico.get(historico.size() - 1).getId();
        return conversas.salvar(conversa, usuario.getId(), requisicao, texto, resposta, ultima);
    }
}
