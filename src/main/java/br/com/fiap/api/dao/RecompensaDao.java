package br.com.fiap.api.dao;

import br.com.fiap.api.model.Recompensa;
import br.com.fiap.api.exception.EntidadeNaoEncontradaException;
import org.springframework.stereotype.Repository;
import br.com.fiap.api.factory.ConnectionFactory;
import java.sql.*;
import java.util.*;

@Repository
public class RecompensaDao {
    private final ConnectionFactory factory;
    private final String SELECT_SQL = "select * from tb_recompensa order by cd_recompensa";
    private final String INSERT_SQL = "insert into tb_recompensa (cd_recompensa, nm_nome, ds_descricao, nr_pontos, st_ativo) values (sq_tb_recompensa.nextval, ?, ?, ?, ?)";
    public RecompensaDao(ConnectionFactory factory) { this.factory = factory; }

    public List<Recompensa> listar() throws SQLException {
        try (Connection conexao = factory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(SELECT_SQL);
             ResultSet rs = stmt.executeQuery()) {
            List<Recompensa> lista = new ArrayList<>();
            while (rs.next()) lista.add(getRecompensa(rs));
            return lista;
        }
    }
    public Recompensa buscar(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = factory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement("select * from tb_recompensa where cd_recompensa = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) throw new EntidadeNaoEncontradaException("Recompensa não encontrada");
                return getRecompensa(rs);
            }
        }
    }
    public Recompensa buscarBloqueando(Connection conexao, int id) throws SQLException, EntidadeNaoEncontradaException {
        try (PreparedStatement stmt = conexao.prepareStatement("select * from tb_recompensa where cd_recompensa=? for update")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) throw new EntidadeNaoEncontradaException("Recompensa não encontrada");
                return getRecompensa(rs);
            }
        }
    }

    private Recompensa getRecompensa(ResultSet rs) throws SQLException {
        Recompensa item = new Recompensa();
        item.setId(rs.getInt("cd_recompensa"));
        item.setNome(rs.getString("nm_nome"));
        item.setDescricao(rs.getString("ds_descricao"));
        item.setPontos(rs.getInt("nr_pontos"));
        item.setAtivo(rs.getInt("st_ativo"));
        return item;
    }
    public void cadastrar(Recompensa item) throws SQLException {
        try (Connection conexao = factory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(INSERT_SQL, new String[]{"cd_recompensa"})) {
            preencher(stmt, item);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) { if (rs.next()) item.setId(rs.getInt(1)); }
        }
    }
    private void preencher(PreparedStatement stmt, Recompensa item) throws SQLException {
        stmt.setString(1, item.getNome()); stmt.setString(2, item.getDescricao());
        stmt.setInt(3, item.getPontos()); stmt.setInt(4, item.getAtivo());
    }
    public void atualizar(Recompensa item) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = factory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement("update tb_recompensa set nm_nome=?, ds_descricao=?, nr_pontos=?, st_ativo=? where cd_recompensa=?")) {
            preencher(stmt, item); stmt.setInt(5, item.getId());
            if (stmt.executeUpdate()==0) throw new EntidadeNaoEncontradaException("Recompensa não encontrada");
        }
    }
    // Exclusão física. O Oracle impede apagar itens presentes no histórico.
    public void remover(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = factory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement("delete from tb_recompensa where cd_recompensa=?")) {
            stmt.setInt(1,id);
            if(stmt.executeUpdate()==0) throw new EntidadeNaoEncontradaException("Recompensa não encontrada");
        }
    }
}
