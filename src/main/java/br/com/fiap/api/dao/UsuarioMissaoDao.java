package br.com.fiap.api.dao;

import br.com.fiap.api.model.*;
import br.com.fiap.api.factory.ConnectionFactory;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UsuarioMissaoDao {
    private final ConnectionFactory factory;

    public UsuarioMissaoDao(ConnectionFactory factory) {
        this.factory = factory;
    }

    public void cadastrar(Connection conexao, UsuarioMissao registro) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement(
                "insert into tb_usuario_missao (cd_registro,cd_usuario,cd_missao,nr_pontos,dt_registro) values (sq_tb_usuario_missao.nextval,?,?,?,sysdate)")) {
            stmt.setInt(1, registro.getUsuario().getId());
            stmt.setInt(2, registro.getMissao().getId());
            stmt.setInt(3, registro.getPontos());
            stmt.executeUpdate();
        }
    }

    public List<UsuarioMissao> listarPorUsuario(int idUsuario) throws SQLException {
        try (Connection conexao = factory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(
                     "select * from tb_usuario_missao where cd_usuario=? order by cd_registro desc")) {
            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                List<UsuarioMissao> lista = new ArrayList<>();
                while (rs.next()) {
                    UsuarioMissao registro = new UsuarioMissao();
                    registro.setId(rs.getInt("cd_registro"));
                    Usuario usuario = new Usuario();
                    usuario.setId(rs.getInt("cd_usuario"));
                    registro.setUsuario(usuario);
                    Missao item = new Missao();
                    item.setId(rs.getInt("cd_missao"));
                    registro.setMissao(item);
                    registro.setPontos(rs.getInt("nr_pontos"));
                    registro.setData(rs.getTimestamp("dt_registro").toLocalDateTime());
                    lista.add(registro);
                }
                return lista;
            }
        }
    }
}
