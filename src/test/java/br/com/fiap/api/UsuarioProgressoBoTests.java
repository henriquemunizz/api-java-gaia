package br.com.fiap.api;

import br.com.fiap.api.bo.UsuarioProgressoBo;
import br.com.fiap.api.dao.*;
import br.com.fiap.api.factory.ConnectionFactory;
import br.com.fiap.api.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.sql.Connection;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioProgressoBoTests {
    private <T> T criarMock(Class<T> classe) {
        return mock(classe, withSettings().mockMaker("mock-maker-subclass"));
    }

    @Test void mesmaMissaoPodeSomarPontosDuasVezes() throws Exception {
        ConnectionFactory factory = criarMock(ConnectionFactory.class);
        Connection conexao = criarMock(Connection.class);
        UsuarioProgressoDao progressoDao = criarMock(UsuarioProgressoDao.class);
        MissaoDao missaoDao = criarMock(MissaoDao.class);
        UsuarioMissaoDao historico = criarMock(UsuarioMissaoDao.class);
        when(factory.getConnection()).thenReturn(conexao);
        UsuarioProgresso progresso = progresso(0);
        when(progressoDao.buscar(conexao, 1, true)).thenReturn(progresso);
        Missao missao = new Missao();
        missao.setAtivo(1); missao.setPontos(20); missao.setId(2);
        when(missaoDao.buscarBloqueando(conexao, 2)).thenReturn(missao);
        UsuarioProgressoBo bo = new UsuarioProgressoBo(factory, progressoDao, missaoDao,
                criarMock(RecompensaDao.class), historico, criarMock(UsuarioRecompensaDao.class));
        bo.concluir(1, 2);
        bo.concluir(1, 2);
        assertEquals(40, progresso.getSaldo());
        assertEquals(40, progresso.getPontosTotais());
        assertEquals(2, progresso.getMissoesConcluidas());
        verify(historico, times(2)).cadastrar(eq(conexao), any(UsuarioMissao.class));
        verify(conexao, times(2)).commit();
    }

    @Test void saldoInsuficienteNaoGravaHistorico() throws Exception {
        ConnectionFactory factory = criarMock(ConnectionFactory.class);
        Connection conexao = criarMock(Connection.class);
        UsuarioProgressoDao progressoDao = criarMock(UsuarioProgressoDao.class);
        RecompensaDao recompensaDao = criarMock(RecompensaDao.class);
        UsuarioRecompensaDao historico = criarMock(UsuarioRecompensaDao.class);
        when(factory.getConnection()).thenReturn(conexao);
        when(progressoDao.buscar(conexao, 1, true)).thenReturn(progresso(10));
        Recompensa recompensa = new Recompensa();
        recompensa.setAtivo(1); recompensa.setPontos(50);
        when(recompensaDao.buscarBloqueando(conexao, 2)).thenReturn(recompensa);
        UsuarioProgressoBo bo = new UsuarioProgressoBo(factory, progressoDao, criarMock(MissaoDao.class),
                recompensaDao, criarMock(UsuarioMissaoDao.class), historico);
        assertEquals(409, assertThrows(ResponseStatusException.class, () -> bo.resgatar(1, 2)).getStatusCode().value());
        verifyNoInteractions(historico);
        verify(progressoDao, never()).atualizar(any(), any());
        verify(conexao).rollback();
        verify(conexao, never()).commit();
    }

    @Test void falhaNoHistoricoReverteTransacao() throws Exception {
        ConnectionFactory factory = criarMock(ConnectionFactory.class);
        Connection conexao = criarMock(Connection.class);
        UsuarioProgressoDao progressoDao = criarMock(UsuarioProgressoDao.class);
        MissaoDao missaoDao = criarMock(MissaoDao.class);
        UsuarioMissaoDao historico = criarMock(UsuarioMissaoDao.class);
        when(factory.getConnection()).thenReturn(conexao);
        when(progressoDao.buscar(conexao, 1, true)).thenReturn(progresso(0));
        Missao missao = new Missao();
        missao.setAtivo(1); missao.setPontos(20);
        when(missaoDao.buscarBloqueando(conexao, 2)).thenReturn(missao);
        doThrow(new SQLException("Falha simulada")).when(historico).cadastrar(eq(conexao), any(UsuarioMissao.class));
        UsuarioProgressoBo bo = new UsuarioProgressoBo(factory, progressoDao, missaoDao,
                criarMock(RecompensaDao.class), historico, criarMock(UsuarioRecompensaDao.class));
        assertThrows(SQLException.class, () -> bo.concluir(1, 2));
        verify(conexao).rollback();
        verify(conexao, never()).commit();
        verify(progressoDao, never()).atualizar(any(), any());
    }

    private UsuarioProgresso progresso(int saldo) {
        Usuario usuario = new Usuario(); usuario.setId(1);
        UsuarioProgresso progresso = new UsuarioProgresso();
        progresso.setUsuario(usuario);
        progresso.setSaldo(saldo); progresso.setPontosTotais(saldo);
        return progresso;
    }
}
