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
        for (Mensagem mensagem : historico) {
            if (mensagem.getRequisicaoId().equals(requisicao)) {
                if (!mensagem.getTexto().equals(texto))
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Identificador de envio já utilizado");
                return historico;
            }
        }
        if (historico.size() >= 400)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esta conversa atingiu o limite. Comece uma nova conversa.");

        Map<String, Object> contexto = montarContexto(usuario, historico, texto);
        String resposta = n8n.responder(contexto);
        int ultima = historico.isEmpty() ? 0 : historico.get(historico.size() - 1).getId();
        return conversas.salvar(conversa, usuario.getId(), requisicao, texto, resposta, ultima);
    }

    private Map<String, Object> montarContexto(Usuario usuario, List<Mensagem> historico, String texto)
            throws SQLException, EntidadeNaoEncontradaException {
        return Map.of(
                "usuario", Map.of("nomeUsuario", usuario.getNomeUsuario()),
                "progresso", mapper.map(progressos.buscar(usuario.getId()), ProgressoResponseDto.class),
                "missoes", missoes.listar().stream().filter(missao -> missao.getAtivo() == 1).limit(100)
                        .map(missao -> mapper.map(missao, MissaoResponseDto.class)).toList(),
                "recompensas", recompensas.listar().stream().filter(recompensa -> recompensa.getAtivo() == 1).limit(100)
                        .map(recompensa -> mapper.map(recompensa, RecompensaResponseDto.class)).toList(),
                "historico", historico.stream().skip(Math.max(0, historico.size() - 20))
                        .map(mensagem -> Map.of("autor", mensagem.getAutor(), "texto", mensagem.getTexto())).toList(),
                "mensagem", texto);
    }
}
