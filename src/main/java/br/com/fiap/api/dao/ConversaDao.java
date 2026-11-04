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

    public ConversaDao(ConnectionFactory factory) {
        this.factory = factory;
    }

    public List<Conversa> listar(int usuario) throws SQLException {
        try (Connection conexao = factory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement("select * from tb_conversa where cd_usuario=? order by cd_conversa desc")) {
            stmt.setInt(1, usuario);
            try (ResultSet resultadoConsulta = stmt.executeQuery()) {
                List<Conversa> lista = new ArrayList<>();
                while (resultadoConsulta.next()) {
                    lista.add(conversa(resultadoConsulta));
                }
                return lista;
            }
        }
    }

    private Conversa conversa(ResultSet resultadoConsulta) throws SQLException {
        Conversa conversa = new Conversa();
        conversa.setId(resultadoConsulta.getInt("cd_conversa"));
        conversa.setTitulo(resultadoConsulta.getString("nm_titulo"));
        conversa.setData(resultadoConsulta.getTimestamp("dt_criacao").toLocalDateTime().toString());
        return conversa;
    }

    public Conversa criar(int usuario) throws SQLException {
        try (Connection conexao = factory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(
                     "insert into tb_conversa (cd_conversa,cd_usuario) values (sq_tb_conversa.nextval,?)", new String[]{"cd_conversa"})) {
            stmt.setInt(1, usuario);
            stmt.executeUpdate();
            try (ResultSet resultadoConsulta = stmt.getGeneratedKeys()) {
                if (!resultadoConsulta.next()) {
                    throw new SQLException("ID da conversa não retornado");
                }
                return buscar(conexao, resultadoConsulta.getInt(1), usuario, false);
            }
        }
    }

    private Conversa buscar(Connection conexao, int id, int usuario, boolean bloquear) throws SQLException {
        String sql = "select * from tb_conversa where cd_conversa=? and cd_usuario=?";
        if (bloquear) {
            sql += " for update";
        }
        try (PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setInt(2, usuario);
            try (ResultSet resultadoConsulta = stmt.executeQuery()) {
                if (!resultadoConsulta.next()) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversa não encontrada");
                }
                return conversa(resultadoConsulta);
            }
        }
    }

    public List<Mensagem> mensagens(int id, int usuario) throws SQLException {
        try (Connection conexao = factory.getConnection()) {
            buscar(conexao, id, usuario, false);
            return mensagens(conexao, id);
        }
    }

    private List<Mensagem> mensagens(Connection conexao, int id) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement("select * from tb_mensagem where cd_conversa=? order by cd_mensagem")) {
            stmt.setInt(1, id);
            try (ResultSet resultadoConsulta = stmt.executeQuery()) {
                List<Mensagem> lista = new ArrayList<>();
                while (resultadoConsulta.next()) {
                    Mensagem mensagem = new Mensagem();
                    mensagem.setId(resultadoConsulta.getInt("cd_mensagem"));
                    mensagem.setAutor(resultadoConsulta.getString("ds_autor"));
                    mensagem.setTexto(resultadoConsulta.getString("ds_texto"));
                    mensagem.setRequisicaoId(resultadoConsulta.getString("ds_requisicao"));
                    mensagem.setData(resultadoConsulta.getTimestamp("dt_envio").toLocalDateTime().toString());
                    lista.add(mensagem);
                }
                return lista;
            }
        }
    }

    // Grava pergunta e resposta juntas. O bloqueio dura somente a gravação, nunca a chamada ao n8n.
    public List<Mensagem> salvar(int id, int usuario, String requisicao, String texto, String resposta, int ultimaMensagem) throws SQLException {
        try (Connection conexao = factory.getConnection()) {
            conexao.setAutoCommit(false);
            try {
                buscar(conexao, id, usuario, true);
                List<Mensagem> atuais = mensagens(conexao, id);
                for (Mensagem mensagem : atuais) {
                    if (mensagem.getRequisicaoId().equals(requisicao)) {
                        if (!mensagem.getTexto().equals(texto))
                            throw new ResponseStatusException(HttpStatus.CONFLICT, "Identificador de envio já utilizado");
                        conexao.commit();
                        return atuais;
                    }
                }
                int ultimaAtual = atuais.isEmpty() ? 0 : atuais.get(atuais.size() - 1).getId();
                if (ultimaAtual != ultimaMensagem)
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "A conversa mudou em outra janela. Atualize antes de enviar novamente.");
                inserir(conexao, id, requisicao, "usuario", texto);
                inserir(conexao, id, requisicao, "gaia", resposta);
                if (atuais.isEmpty()) {
                    try (PreparedStatement stmt = conexao.prepareStatement("update tb_conversa set nm_titulo=? where cd_conversa=?")) {
                        stmt.setString(1, texto.substring(0, texto.offsetByCodePoints(0, Math.min(60, texto.codePointCount(0, texto.length())))));
                        stmt.setInt(2, id);
                        stmt.executeUpdate();
                    }
                }
                List<Mensagem> resultado = mensagens(conexao, id);
                conexao.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                conexao.rollback();
                throw e;
            }
        }
    }

    private void inserir(Connection conexao, int conversa, String requisicao, String autor, String texto) throws SQLException {
        try (PreparedStatement stmt = conexao.prepareStatement(
                "insert into tb_mensagem (cd_mensagem,cd_conversa,ds_autor,ds_texto,ds_requisicao) values (sq_tb_mensagem.nextval,?,?,?,?)")) {
            stmt.setInt(1, conversa);
            stmt.setString(2, autor);
            stmt.setString(3, texto);
            stmt.setString(4, requisicao);
            stmt.executeUpdate();
        }
    }
}
