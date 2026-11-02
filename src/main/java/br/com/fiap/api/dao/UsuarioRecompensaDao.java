package br.com.fiap.api.dao;

import br.com.fiap.api.model.*;
import br.com.fiap.api.factory.ConnectionFactory;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UsuarioRecompensaDao {
    private final ConnectionFactory factory;

    public UsuarioRecompensaDao(ConnectionFactory factory) {
        this.factory = factory;
    }

    public void cadastrar(Connection conexao, UsuarioRecompensa registro) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement(
                "insert into tb_usuario_recompensa (cd_registro,cd_usuario,cd_recompensa,nr_pontos,dt_registro) values (sq_tb_usuario_recompensa.nextval,?,?,?,sysdate)")) {
            stmt.setInt(1, registro.getUsuario().getId());
            stmt.setInt(2, registro.getRecompensa().getId());
            stmt.setInt(3, registro.getPontos());
            stmt.executeUpdate();
        }
    }

    public List<UsuarioRecompensa> listarPorUsuario(int idUsuario) throws SQLException {
        try (Connection conexao = factory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(
                     "select * from tb_usuario_recompensa where cd_usuario=? order by cd_registro desc")) {
            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                List<UsuarioRecompensa> lista = new ArrayList<>();
                while (rs.next()) {
                    UsuarioRecompensa registro = new UsuarioRecompensa();
                    registro.setId(rs.getInt("cd_registro"));
                    Usuario usuario = new Usuario();
                    usuario.setId(rs.getInt("cd_usuario"));
                    registro.setUsuario(usuario);
                    Recompensa item = new Recompensa();
                    item.setId(rs.getInt("cd_recompensa"));
                    registro.setRecompensa(item);
                    registro.setPontos(rs.getInt("nr_pontos"));
                    registro.setData(rs.getTimestamp("dt_registro").toLocalDateTime());
                    lista.add(registro);
                }
                return lista;
            }
        }
    }
}
