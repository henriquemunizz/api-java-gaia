package br.com.fiap.api.config;

import br.com.fiap.api.dto.ChatRespostaDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import java.time.Duration;
import java.util.Map;

@Component
public class N8nChat {
    private final String url;
    private final String chave;
    private final RestClient client;

    public N8nChat(@Value("${gaia.chat.webhook:}") String url, @Value("${gaia.chat.chave:}") String chave) {
        this.url = url;
        this.chave = chave;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(45));
        client = RestClient.builder().requestFactory(factory).build();
    }

    public String responder(Map<String, Object> contexto) {
        if (url.isBlank() || chave.isBlank())
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "O chat ainda não está configurado. Tente mais tarde.");
        try {
            ChatRespostaDto dto = client.post().uri(url).header("X-Gaia-Key", chave)
                    .body(contexto).retrieve().body(ChatRespostaDto.class);
            if (dto == null || dto.getResposta() == null || dto.getResposta().isBlank() || dto.getResposta().length() > 6000)
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "A Gaia retornou uma resposta inválida. Tente novamente.");
            return dto.getResposta().trim();
        } catch (RestClientException e) {
            // Não registra URL, chave nem conteúdo privado da conversa nos logs.
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Não foi possível obter a resposta da Gaia. Sua mensagem continua no campo para tentar novamente.");
        }
    }
}
