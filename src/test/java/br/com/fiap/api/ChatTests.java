package br.com.fiap.api;

import br.com.fiap.api.bo.ChatBo;
import br.com.fiap.api.config.N8nChat;
import br.com.fiap.api.dao.*;
import br.com.fiap.api.dto.ChatMensagemRequestDto;
import br.com.fiap.api.model.*;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ChatTests {
    private <T> T simular(Class<T> tipo) { return mock(tipo, withSettings().mockMaker("mock-maker-subclass")); }
    private final ConversaDao conversas = simular(ConversaDao.class);
    private final MissaoDao missoes = simular(MissaoDao.class);
    private final RecompensaDao recompensas = simular(RecompensaDao.class);
    private final UsuarioProgressoDao progressos = simular(UsuarioProgressoDao.class);
    private final N8nChat n8n = simular(N8nChat.class);
    private final ChatBo bo = new ChatBo(conversas, missoes, recompensas, progressos, n8n, new ModelMapper());

    private Usuario usuario() {
        Usuario u = new Usuario(); u.setId(7); u.setNomeUsuario("teste"); u.setSenha("NAO-ENVIAR-HASH"); u.setPerfil(1); return u;
    }
    private ChatMensagemRequestDto entrada() {
        ChatMensagemRequestDto dto = new ChatMensagemRequestDto();
        dto.setTexto("Quais missões estão disponíveis?"); dto.setRequisicaoId("cd0538ec-b230-4652-9475-c6e3d4e56001"); return dto;
    }

    @Test void conversaDeOutroUsuarioNaoChegaAoN8nMesmoParaAdmin() throws Exception {
        when(conversas.mensagens(10, 7)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));
        assertThrows(ResponseStatusException.class, () -> bo.enviar(10, usuario(), entrada()));
        verifyNoInteractions(n8n, missoes, recompensas, progressos);
    }

    @Test void contextoUsaSomenteCatalogoAtivoENaoEnviaCredenciais() throws Exception {
        when(conversas.mensagens(10, 7)).thenReturn(List.of());
        Missao ativa = new Missao(); ativa.setId(1); ativa.setNome("Separar papel"); ativa.setAtivo(1);
        Missao inativa = new Missao(); inativa.setId(2); inativa.setAtivo(0);
        Recompensa ativaR = new Recompensa(); ativaR.setId(3); ativaR.setAtivo(1);
        Recompensa inativaR = new Recompensa(); inativaR.setId(4); inativaR.setAtivo(0);
        when(missoes.listar()).thenReturn(List.of(ativa, inativa));
        when(recompensas.listar()).thenReturn(List.of(ativaR, inativaR));
        when(progressos.buscar(7)).thenReturn(new UsuarioProgresso());
        when(n8n.responder(anyMap())).thenAnswer(invocation -> {
            Map<?, ?> contexto = invocation.getArgument(0);
            assertEquals(Map.of("nomeUsuario", "teste"), contexto.get("usuario"));
            assertEquals(1, ((List<?>) contexto.get("missoes")).size());
            assertEquals(1, ((List<?>) contexto.get("recompensas")).size());
            assertFalse(contexto.containsKey("senha"));
            assertFalse(contexto.containsKey("cookie"));
            return "Você pode separar papel.";
        });
        bo.enviar(10, usuario(), entrada());
        verify(conversas).salvar(10, 7, entrada().getRequisicaoId(), entrada().getTexto(), "Você pode separar papel.", 0);
    }

    @Test void reenvioConfirmadoNaoDuplicaMensagemNemChamaModelo() throws Exception {
        Mensagem m = new Mensagem(); m.setRequisicaoId(entrada().getRequisicaoId()); m.setTexto(entrada().getTexto()); m.setAutor("usuario");
        when(conversas.mensagens(10, 7)).thenReturn(List.of(m));
        assertEquals(List.of(m), bo.enviar(10, usuario(), entrada()));
        verifyNoInteractions(n8n);
        ChatMensagemRequestDto diferente = entrada(); diferente.setTexto("Texto diferente");
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> bo.enviar(10, usuario(), diferente)).getStatusCode().value());
    }

    @Test void indisponibilidadeNaoGravaRespostaInventada() throws Exception {
        when(conversas.mensagens(10, 7)).thenReturn(List.of());
        when(progressos.buscar(7)).thenReturn(new UsuarioProgresso());
        when(n8n.responder(anyMap())).thenThrow(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE));
        assertThrows(ResponseStatusException.class, () -> bo.enviar(10, usuario(), entrada()));
        verify(conversas, never()).salvar(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyInt());
    }

    @Test void webhookUsaChaveDeServidorEValidaResposta() throws Exception {
        AtomicReference<String> chave = new AtomicReference<>();
        AtomicReference<String> corpo = new AtomicReference<>();
        AtomicReference<String> resposta = new AtomicReference<>("{\"resposta\":\"Olá, teste!\"}");
        HttpServer servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/chat", exchange -> {
            chave.set(exchange.getRequestHeaders().getFirst("X-Gaia-Key"));
            corpo.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] bytes = resposta.get().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes); exchange.close();
        });
        servidor.start();
        try {
            N8nChat client = new N8nChat("http://localhost:" + servidor.getAddress().getPort() + "/chat", "chave-somente-teste");
            assertEquals("Olá, teste!", client.responder(Map.of("mensagem", "Olá")));
            assertEquals("chave-somente-teste", chave.get());
            assertTrue(corpo.get().contains("mensagem"));
            resposta.set("{\"resposta\":\"\"}");
            assertEquals(502, assertThrows(ResponseStatusException.class, () -> client.responder(Map.of())).getStatusCode().value());
            resposta.set("{\"resposta\":\"" + "x".repeat(6001) + "\"}");
            assertThrows(ResponseStatusException.class, () -> client.responder(Map.of()));
            resposta.set("nao-json");
            assertEquals(503, assertThrows(ResponseStatusException.class, () -> client.responder(Map.of())).getStatusCode().value());
        } finally { servidor.stop(0); }
        assertEquals(503, assertThrows(ResponseStatusException.class, () -> new N8nChat("", "").responder(Map.of())).getStatusCode().value());
    }
}
