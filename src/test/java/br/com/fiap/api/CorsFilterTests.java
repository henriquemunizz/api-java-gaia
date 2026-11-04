package br.com.fiap.api;

import br.com.fiap.api.config.CorsFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

class CorsFilterTests {
    @Test void permiteVercelELocalComCredenciais() throws Exception {
        String vercel = "https://gaia-react-gamma.vercel.app";
        String local = "http://localhost:5173";
        CorsFilter filtro = new CorsFilter(vercel + ", " + local);
        for (String origem : new String[]{vercel, local}) {
            MockHttpServletResponse resposta = consultar(filtro, origem);
            assertEquals(200, resposta.getStatus());
            assertEquals(origem, resposta.getHeader("Access-Control-Allow-Origin"));
            assertEquals("true", resposta.getHeader("Access-Control-Allow-Credentials"));
        }
    }

    @Test void rejeitaOrigemNaoConfigurada() throws Exception {
        CorsFilter filtro = new CorsFilter("https://gaia-react-gamma.vercel.app,http://localhost:5173");
        MockHttpServletResponse resposta = consultar(filtro, "https://outro.example");
        assertEquals(403, resposta.getStatus());
        assertNull(resposta.getHeader("Access-Control-Allow-Origin"));
    }

    @Test void mantemConfiguracaoComUmaOrigem() throws Exception {
        String origem = "http://localhost:5173";
        MockHttpServletResponse resposta = consultar(new CorsFilter(origem), origem);
        assertEquals(200, resposta.getStatus());
        assertEquals(origem, resposta.getHeader("Access-Control-Allow-Origin"));
    }

    private MockHttpServletResponse consultar(CorsFilter filtro, String origem) throws Exception {
        MockHttpServletRequest requisicao = new MockHttpServletRequest("OPTIONS", "/usuarios/login");
        requisicao.addHeader("Origin", origem);
        requisicao.addHeader("Access-Control-Request-Method", "POST");
        requisicao.addHeader("Access-Control-Request-Headers", "Content-Type");
        MockHttpServletResponse resposta = new MockHttpServletResponse();
        filtro.doFilter(requisicao, resposta, new MockFilterChain());
        return resposta;
    }
}
