package br.com.fiap.api.dao;

import br.com.fiap.api.model.Usuario;
import br.com.fiap.api.exception.EntidadeNaoEncontradaException;
import br.com.fiap.api.factory.ConnectionFactory;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UsuarioDao {
    private final ConnectionFactory factory;

    public UsuarioDao(ConnectionFactory factory) {
        this.factory = factory;
    }

    private Usuario getUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("cd_usuario"));
        usuario.setNomeUsuario(rs.getString("nm_usuario"));
        usuario.setSenha(rs.getString("ds_senha"));
        usuario.setPerfil(rs.getInt("ds_perfil"));
        return usuario;
    }

    public Usuario buscarPorNome(String nome) throws SQLException {
        try (Connection conexao = factory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement("select * from tb_usuario where nm_usuario=?")) {
            stmt.setString(1, nome);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? getUsuario(rs) : null;
            }
        }
    }

    public Usuario buscar(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = factory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement("select * from tb_usuario where cd_usuario=?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) throw new EntidadeNaoEncontradaException("Usuário não encontrado");
                return getUsuario(rs);
            }
        }
    }

    public List<Usuario> listar() throws SQLException {
        try (Connection conexao = factory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement("select * from tb_usuario order by cd_usuario");
             ResultSet rs = stmt.executeQuery()) {
            List<Usuario> lista = new ArrayList<>();
            while (rs.next()) lista.add(getUsuario(rs));
            return lista;
        }
    }

    public void cadastrar(Usuario usuario) throws SQLException {
        try (Connection conexao = factory.getConnection()) {
            conexao.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conexao.prepareStatement(
                        "insert into tb_usuario (cd_usuario,nm_usuario,ds_senha,ds_perfil) values (sq_tb_usuario.nextval,?,?,?)",
                        new String[]{"cd_usuario"})) {
                    stmt.setString(1, usuario.getNomeUsuario());
                    stmt.setString(2, usuario.getSenha());
                    stmt.setInt(3, usuario.getPerfil());
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("ID do usuário não retornado");
                        usuario.setId(rs.getInt(1));
                    }
                }
                try (PreparedStatement stmt = conexao.prepareStatement(
                        "insert into tb_usuario_progresso (cd_usuario,nr_pontos_totais,nr_saldo,nr_missoes) values (?,0,0,0)")) {
                    stmt.setInt(1, usuario.getId());
                    stmt.executeUpdate();
                }
                conexao.commit();
            } catch (SQLException | RuntimeException e) {
                conexao.rollback();
                throw e;
            }
        }
    }

    public void atualizar(Usuario usuario) throws SQLException, EntidadeNaoEncontradaException {
        // Senha nula significa manter a atual; edição de conta não altera o perfil.
        String sql = usuario.getSenha() == null
                ? "update tb_usuario set nm_usuario=? where cd_usuario=?"
                : "update tb_usuario set nm_usuario=?, ds_senha=? where cd_usuario=?";
        try (Connection conexao = factory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNomeUsuario());
            if (usuario.getSenha() == null) {
                stmt.setInt(2, usuario.getId());
            } else {
                stmt.setString(2, usuario.getSenha());
                stmt.setInt(3, usuario.getId());
            }
            if (stmt.executeUpdate() == 0)
                throw new EntidadeNaoEncontradaException("Usuário não encontrado");
        }
    }

    public void remover(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = factory.getConnection()) {
            conexao.setAutoCommit(false);
            try {
                // Mesma ordem de bloqueio da conclusão/resgate: progresso antes do histórico.
                try (PreparedStatement stmt = conexao.prepareStatement(
                        "select cd_usuario from tb_usuario_progresso where cd_usuario=? for update")) {
                    stmt.setInt(1, id);
                    try (ResultSet rs = stmt.executeQuery()) { rs.next(); }
                }
                excluirDependentes(conexao, "delete from tb_usuario_missao where cd_usuario=?", id);
                excluirDependentes(conexao, "delete from tb_usuario_recompensa where cd_usuario=?", id);
                excluirDependentes(conexao, "delete from tb_usuario_progresso where cd_usuario=?", id);
                try (PreparedStatement stmt = conexao.prepareStatement("delete from tb_usuario where cd_usuario=?")) {
                    stmt.setInt(1, id);
                    if (stmt.executeUpdate() == 0)
                        throw new EntidadeNaoEncontradaException("Usuário não encontrado");
                }
                conexao.commit();
            } catch (SQLException | RuntimeException | EntidadeNaoEncontradaException e) {
                conexao.rollback();
                throw e;
            }
        }
    }

    private void excluirDependentes(Connection conexao, String sql, int id) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
