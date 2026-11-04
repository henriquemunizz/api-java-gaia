package br.com.fiap.api.dao;

import br.com.fiap.api.factory.ConnectionFactory;
import br.com.fiap.api.model.Conversa;
import br.com.fiap.api.model.Mensagem;
import org.springframework.stereotype.Repository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ConversaDao {
    private final ConnectionFactory factory;
    public ConversaDao(ConnectionFactory factory) { this.factory = factory; }

    public List<Conversa> listar(int usuario) throws SQLException {
        try (Connection c = factory.getConnection();
             PreparedStatement s = c.prepareStatement("select * from tb_conversa where cd_usuario=? order by cd_conversa desc")) {
            s.setInt(1, usuario);
            try (ResultSet r = s.executeQuery()) {
                List<Conversa> lista = new ArrayList<>();
                while (r.next()) lista.add(conversa(r));
                return lista;
            }
        }
    }

    private Conversa conversa(ResultSet r) throws SQLException {
        Conversa conversa = new Conversa();
        conversa.setId(r.getInt("cd_conversa"));
        conversa.setTitulo(r.getString("nm_titulo"));
        conversa.setData(r.getTimestamp("dt_criacao").toLocalDateTime().toString());
        return conversa;
    }

    public Conversa criar(int usuario) throws SQLException {
        try (Connection c = factory.getConnection();
             PreparedStatement s = c.prepareStatement(
                     "insert into tb_conversa (cd_conversa,cd_usuario) values (sq_tb_conversa.nextval,?)", new String[]{"cd_conversa"})) {
            s.setInt(1, usuario);
            s.executeUpdate();
            try (ResultSet r = s.getGeneratedKeys()) {
                if (!r.next()) throw new SQLException("ID da conversa não retornado");
                return buscar(c, r.getInt(1), usuario, false);
            }
        }
    }

    private Conversa buscar(Connection c, int id, int usuario, boolean bloquear) throws SQLException {
        String sql = "select * from tb_conversa where cd_conversa=? and cd_usuario=?";
        if (bloquear) sql += " for update";
        try (PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, id);
            s.setInt(2, usuario);
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversa não encontrada");
                return conversa(r);
            }
        }
    }

    public List<Mensagem> mensagens(int id, int usuario) throws SQLException {
        try (Connection c = factory.getConnection()) {
            buscar(c, id, usuario, false);
            return mensagens(c, id);
        }
    }

    private List<Mensagem> mensagens(Connection c, int id) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("select * from tb_mensagem where cd_conversa=? order by cd_mensagem")) {
            s.setInt(1, id);
            try (ResultSet r = s.executeQuery()) {
                List<Mensagem> lista = new ArrayList<>();
                while (r.next()) {
                    Mensagem m = new Mensagem();
                    m.setId(r.getInt("cd_mensagem"));
                    m.setAutor(r.getString("ds_autor"));
                    m.setTexto(r.getString("ds_texto"));
                    m.setRequisicaoId(r.getString("ds_requisicao"));
                    m.setData(r.getTimestamp("dt_envio").toLocalDateTime().toString());
                    lista.add(m);
                }
                return lista;
            }
        }
    }

    // Grava pergunta e resposta juntas. O bloqueio dura somente a gravação, nunca a chamada ao n8n.
    public List<Mensagem> salvar(int id, int usuario, String requisicao, String texto, String resposta, int ultimaMensagem) throws SQLException {
        try (Connection c = factory.getConnection()) {
            c.setAutoCommit(false);
            try {
                buscar(c, id, usuario, true);
                List<Mensagem> atuais = mensagens(c, id);
                for (Mensagem m : atuais) {
                    if (m.getRequisicaoId().equals(requisicao)) {
                        if (!m.getTexto().equals(texto))
                            throw new ResponseStatusException(HttpStatus.CONFLICT, "Identificador de envio já utilizado");
                        c.commit();
                        return atuais;
                    }
                }
                int ultimaAtual = atuais.isEmpty() ? 0 : atuais.get(atuais.size() - 1).getId();
                if (ultimaAtual != ultimaMensagem)
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "A conversa mudou em outra janela. Atualize antes de enviar novamente.");
                inserir(c, id, requisicao, "usuario", texto);
                inserir(c, id, requisicao, "gaia", resposta);
                if (atuais.isEmpty()) {
                    try (PreparedStatement s = c.prepareStatement("update tb_conversa set nm_titulo=? where cd_conversa=?")) {
                        s.setString(1, texto.substring(0, texto.offsetByCodePoints(0, Math.min(60, texto.codePointCount(0, texto.length())))));
                        s.setInt(2, id);
                        s.executeUpdate();
                    }
                }
                List<Mensagem> resultado = mensagens(c, id);
                c.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private void inserir(Connection c, int conversa, String requisicao, String autor, String texto) throws SQLException {
        try (PreparedStatement s = c.prepareStatement(
                "insert into tb_mensagem (cd_mensagem,cd_conversa,ds_autor,ds_texto,ds_requisicao) values (sq_tb_mensagem.nextval,?,?,?,?)")) {
            s.setInt(1, conversa);
            s.setString(2, autor);
            s.setString(3, texto);
            s.setString(4, requisicao);
            s.executeUpdate();
        }
    }
}
