package br.com.fiap.api.bo;

import br.com.fiap.api.dao.*;
import br.com.fiap.api.model.*;
import br.com.fiap.api.factory.ConnectionFactory;
import br.com.fiap.api.exception.EntidadeNaoEncontradaException;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.sql.Connection;
import java.sql.SQLException;

// BO coordena as regras e a transação; DAOs executam somente SQL e mapeiam os modelos.
@Component
public class UsuarioProgressoBo {
    private final ConnectionFactory factory;
    private final UsuarioProgressoDao progressoDao;
    private final MissaoDao missaoDao;
    private final RecompensaDao recompensaDao;
    private final UsuarioMissaoDao usuarioMissaoDao;
    private final UsuarioRecompensaDao usuarioRecompensaDao;

    public UsuarioProgressoBo(ConnectionFactory factory, UsuarioProgressoDao progressoDao,
                             MissaoDao missaoDao, RecompensaDao recompensaDao,
                             UsuarioMissaoDao usuarioMissaoDao, UsuarioRecompensaDao usuarioRecompensaDao) {
        this.factory = factory;
        this.progressoDao = progressoDao;
        this.missaoDao = missaoDao;
        this.recompensaDao = recompensaDao;
        this.usuarioMissaoDao = usuarioMissaoDao;
        this.usuarioRecompensaDao = usuarioRecompensaDao;
    }

    public UsuarioProgresso concluir(int usuario, int idMissao) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = factory.getConnection()) {
            conexao.setAutoCommit(false);
            try {
                UsuarioProgresso progresso = progressoDao.buscar(conexao, usuario, true);
                Missao missao = missaoDao.buscarBloqueando(conexao, idMissao);
                if (missao.getAtivo() != 1)
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Missão inativa");
                progresso.setPontosTotais(somar(progresso.getPontosTotais(), missao.getPontos()));
                progresso.setSaldo(somar(progresso.getSaldo(), missao.getPontos()));
                progresso.setMissoesConcluidas(somar(progresso.getMissoesConcluidas(), 1));
                UsuarioMissao registro = new UsuarioMissao();
                registro.setUsuario(progresso.getUsuario());
                registro.setMissao(missao);
                registro.setPontos(missao.getPontos());
                usuarioMissaoDao.cadastrar(conexao, registro);
                progressoDao.atualizar(conexao, progresso);
                conexao.commit();
                return progresso;
            } catch (SQLException | EntidadeNaoEncontradaException | RuntimeException e) {
                conexao.rollback();
                throw e;
            }
        }
    }

    public UsuarioProgresso resgatar(int usuario, int idRecompensa) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = factory.getConnection()) {
            conexao.setAutoCommit(false);
            try {
                // O bloqueio impede gastar o mesmo saldo em dois resgates simultâneos.
                UsuarioProgresso progresso = progressoDao.buscar(conexao, usuario, true);
                Recompensa recompensa = recompensaDao.buscarBloqueando(conexao, idRecompensa);
                if (recompensa.getAtivo() != 1)
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Recompensa inativa");
                if (progresso.getSaldo() < recompensa.getPontos())
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Pontos insuficientes");
                progresso.setSaldo(progresso.getSaldo() - recompensa.getPontos());
                UsuarioRecompensa registro = new UsuarioRecompensa();
                registro.setUsuario(progresso.getUsuario());
                registro.setRecompensa(recompensa);
                registro.setPontos(recompensa.getPontos());
                usuarioRecompensaDao.cadastrar(conexao, registro);
                progressoDao.atualizar(conexao, progresso);
                conexao.commit();
                return progresso;
            } catch (SQLException | EntidadeNaoEncontradaException | RuntimeException e) {
                conexao.rollback();
                throw e;
            }
        }
    }

    private int somar(int atual, int quantidade) {
        try {
            return Math.addExact(atual, quantidade);
        } catch (ArithmeticException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Limite de pontos ou conclusões atingido");
        }
    }
}
