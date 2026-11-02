package br.com.fiap.api.dao;

import br.com.fiap.api.model.Usuario;
import br.com.fiap.api.model.UsuarioProgresso;
import br.com.fiap.api.factory.ConnectionFactory;
import br.com.fiap.api.exception.EntidadeNaoEncontradaException;
import org.springframework.stereotype.Repository;
import java.sql.*;

@Repository
public class UsuarioProgressoDao {
    private final ConnectionFactory factory;

    public UsuarioProgressoDao(ConnectionFactory factory) {
        this.factory = factory;
    }

    public UsuarioProgresso buscar(int usuario) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = factory.getConnection()) {
            return buscar(conexao, usuario, false);
        }
    }

    public UsuarioProgresso buscar(Connection conexao, int usuario, boolean bloquear)
            throws SQLException, EntidadeNaoEncontradaException {
        String sql = "select * from tb_usuario_progresso where cd_usuario=?";
        if (bloquear) sql += " for update";
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, usuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) throw new EntidadeNaoEncontradaException("Progresso não encontrado");
                UsuarioProgresso progresso = new UsuarioProgresso();
                Usuario dono = new Usuario();
                dono.setId(usuario);
                progresso.setUsuario(dono);
                progresso.setPontosTotais(rs.getInt("nr_pontos_totais"));
                progresso.setSaldo(rs.getInt("nr_saldo"));
                progresso.setMissoesConcluidas(rs.getInt("nr_missoes"));
                progresso.setDataAtualizacao(rs.getTimestamp("dt_atualizacao").toLocalDateTime());
                return progresso;
            }
        }
    }

    public void atualizar(Connection conexao, UsuarioProgresso progresso) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement(
                "update tb_usuario_progresso set nr_pontos_totais=?, nr_saldo=?, nr_missoes=?, dt_atualizacao=sysdate where cd_usuario=?")) {
            stmt.setInt(1, progresso.getPontosTotais());
            stmt.setInt(2, progresso.getSaldo());
            stmt.setInt(3, progresso.getMissoesConcluidas());
            stmt.setInt(4, progresso.getUsuario().getId());
            if (stmt.executeUpdate() == 0) throw new SQLException("Progresso não encontrado para atualização");
        }
    }
}
