package br.com.fiap.api.dao;

import br.com.fiap.api.model.Missao;
import br.com.fiap.api.exception.EntidadeNaoEncontradaException;
import org.springframework.stereotype.Repository;
import br.com.fiap.api.factory.ConnectionFactory;
import java.sql.*;
import java.util.*;

@Repository
public class MissaoDao {
    private final ConnectionFactory factory;
    private final String SELECT_SQL = "select * from tb_missao order by cd_missao";
    private final String INSERT_SQL = "insert into tb_missao (cd_missao, nm_nome, ds_descricao, nr_pontos, st_ativo, tp_dificuldade) values (sq_tb_missao.nextval, ?, ?, ?, ?, ?)";
    public MissaoDao(ConnectionFactory factory) { this.factory = factory; }

    public List<Missao> listar() throws SQLException {
        try (Connection conexao = factory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(SELECT_SQL);
             ResultSet rs = stmt.executeQuery()) {
            List<Missao> lista = new ArrayList<>();
            while (rs.next()) lista.add(getMissao(rs));
            return lista;
        }
    }
    public Missao buscar(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = factory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement("select * from tb_missao where cd_missao = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) throw new EntidadeNaoEncontradaException("Missao não encontrada");
                return getMissao(rs);
            }
        }
    }
    public Missao buscarBloqueando(Connection conexao, int id) throws SQLException, EntidadeNaoEncontradaException {
        try (PreparedStatement stmt = conexao.prepareStatement("select * from tb_missao where cd_missao=? for update")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) throw new EntidadeNaoEncontradaException("Missao não encontrada");
                return getMissao(rs);
            }
        }
    }

    private Missao getMissao(ResultSet rs) throws SQLException {
        Missao item = new Missao();
        item.setId(rs.getInt("cd_missao"));
        item.setNome(rs.getString("nm_nome"));
        item.setDescricao(rs.getString("ds_descricao"));
        item.setPontos(rs.getInt("nr_pontos"));
        item.setAtivo(rs.getInt("st_ativo"));
        item.setDificuldade(rs.getInt("tp_dificuldade"));
        return item;
    }
    public void cadastrar(Missao item) throws SQLException {
        try (Connection conexao = factory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(INSERT_SQL, new String[]{"cd_missao"})) {
            preencher(stmt, item);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) { if (rs.next()) item.setId(rs.getInt(1)); }
        }
    }
    private void preencher(PreparedStatement stmt, Missao item) throws SQLException {
        stmt.setString(1, item.getNome()); stmt.setString(2, item.getDescricao());
        stmt.setInt(3, item.getPontos()); stmt.setInt(4, item.getAtivo()); stmt.setInt(5, item.getDificuldade());
    }
    public void atualizar(Missao item) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = factory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement("update tb_missao set nm_nome=?, ds_descricao=?, nr_pontos=?, st_ativo=?, tp_dificuldade=? where cd_missao=?")) {
            preencher(stmt, item); stmt.setInt(6, item.getId());
            if (stmt.executeUpdate()==0) throw new EntidadeNaoEncontradaException("Missao não encontrada");
        }
    }
    // Exclusão física. O Oracle impede apagar itens presentes no histórico.
    public void remover(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = factory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement("delete from tb_missao where cd_missao=?")) {
            stmt.setInt(1,id);
            if(stmt.executeUpdate()==0) throw new EntidadeNaoEncontradaException("Missao não encontrada");
        }
    }
}

