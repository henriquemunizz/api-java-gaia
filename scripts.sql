-- ============================================================
-- GAIA - SCHEMA ORACLE COMPLETO
-- Instalação inicial em um schema vazio.
-- Execute manualmente no mesmo schema utilizado pela API.
-- A aplicação não executa este arquivo automaticamente.
--
-- Perfil:     0 = usuário, 1 = administrador
-- Dificuldade: 1 = fácil, 2 = médio, 3 = difícil
-- Ativo:      0 = inativo, 1 = ativo
-- ============================================================

-- 1. USUÁRIOS
CREATE TABLE tb_usuario (
                            cd_usuario NUMBER(10) PRIMARY KEY,
                            nm_usuario VARCHAR2(40) NOT NULL UNIQUE,
                            ds_senha   VARCHAR2(200) NOT NULL,
                            ds_perfil  NUMBER(1) DEFAULT 0 NOT NULL
               CHECK (ds_perfil IN (0, 1))
);

CREATE SEQUENCE sq_tb_usuario
    START WITH 1 INCREMENT BY 1 NOCACHE;


-- 2. PROGRESSO DO USUÁRIO
CREATE TABLE tb_usuario_progresso (
                                      cd_usuario       NUMBER(10) PRIMARY KEY
                     REFERENCES tb_usuario(cd_usuario),
                                      nr_pontos_totais NUMBER(10) DEFAULT 0 NOT NULL
                     CHECK (nr_pontos_totais >= 0),
                                      nr_saldo         NUMBER(10) DEFAULT 0 NOT NULL
                     CHECK (nr_saldo >= 0),
                                      nr_missoes       NUMBER(10) DEFAULT 0 NOT NULL
                     CHECK (nr_missoes >= 0),
                                      dt_atualizacao   DATE DEFAULT SYSDATE NOT NULL,
                                      CONSTRAINT ck_saldo_total
                                          CHECK (nr_saldo <= nr_pontos_totais)
);


-- 3. MISSÕES
CREATE TABLE tb_missao (
                           cd_missao      NUMBER(10) PRIMARY KEY,
                           tp_dificuldade NUMBER(1) NOT NULL
                   CHECK (tp_dificuldade IN (1, 2, 3)),
                           nm_nome        VARCHAR2(100) NOT NULL,
                           ds_descricao   VARCHAR2(500) NOT NULL,
                           nr_pontos      NUMBER(9) NOT NULL
                   CHECK (nr_pontos > 0),
                           st_ativo       NUMBER(1) DEFAULT 1 NOT NULL
                   CHECK (st_ativo IN (0, 1))
);

CREATE SEQUENCE sq_tb_missao
    START WITH 1 INCREMENT BY 1 NOCACHE;


-- 4. HISTÓRICO DE MISSÕES CONCLUÍDAS
-- Permite concluir a mesma missão várias vezes.
-- Os pontos ficam registrados com o valor da conclusão.
CREATE TABLE tb_usuario_missao (
                                   cd_registro NUMBER(10) PRIMARY KEY,
                                   cd_usuario  NUMBER(10) NOT NULL
                REFERENCES tb_usuario(cd_usuario),
                                   cd_missao   NUMBER(10) NOT NULL
                REFERENCES tb_missao(cd_missao),
                                   nr_pontos   NUMBER(9) NOT NULL
                CHECK (nr_pontos > 0),
                                   dt_registro DATE DEFAULT SYSDATE NOT NULL
);

CREATE SEQUENCE sq_tb_usuario_missao
    START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE INDEX ix_usuario_missao
    ON tb_usuario_missao(cd_usuario);

CREATE INDEX ix_missao_historico
    ON tb_usuario_missao(cd_missao);


-- 5. RECOMPENSAS
CREATE TABLE tb_recompensa (
                               cd_recompensa NUMBER(10) PRIMARY KEY,
                               nm_nome       VARCHAR2(100) NOT NULL,
                               ds_descricao  VARCHAR2(500) NOT NULL,
                               nr_pontos     NUMBER(9) NOT NULL
                  CHECK (nr_pontos > 0),
                               st_ativo      NUMBER(1) DEFAULT 1 NOT NULL
                  CHECK (st_ativo IN (0, 1))
);

CREATE SEQUENCE sq_tb_recompensa
    START WITH 1 INCREMENT BY 1 NOCACHE;


-- 6. HISTÓRICO DE RECOMPENSAS RESGATADAS
-- Permite resgatar a mesma recompensa várias vezes.
-- Os pontos ficam registrados com o custo do resgate.
CREATE TABLE tb_usuario_recompensa (
                                       cd_registro   NUMBER(10) PRIMARY KEY,
                                       cd_usuario    NUMBER(10) NOT NULL
                  REFERENCES tb_usuario(cd_usuario),
                                       cd_recompensa NUMBER(10) NOT NULL
                  REFERENCES tb_recompensa(cd_recompensa),
                                       nr_pontos     NUMBER(9) NOT NULL
                  CHECK (nr_pontos > 0),
                                       dt_registro   DATE DEFAULT SYSDATE NOT NULL
);

CREATE SEQUENCE sq_tb_usuario_recompensa
    START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE INDEX ix_usuario_recompensa
    ON tb_usuario_recompensa(cd_usuario);

CREATE INDEX ix_recompensa_historico
    ON tb_usuario_recompensa(cd_recompensa);


-- 7. CONVERSAS COM A GAIA
-- A exclusão do usuário também exclui suas conversas.
CREATE TABLE tb_conversa (
                             cd_conversa NUMBER(10) PRIMARY KEY,
                             cd_usuario  NUMBER(10) NOT NULL
                REFERENCES tb_usuario(cd_usuario) ON DELETE CASCADE,
                             nm_titulo   VARCHAR2(80 CHAR) DEFAULT 'Nova conversa' NOT NULL,
                             dt_criacao  TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL
);

CREATE SEQUENCE sq_tb_conversa
    START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE INDEX ix_conversa_usuario
    ON tb_conversa(cd_usuario);


-- 8. MENSAGENS DAS CONVERSAS
-- A exclusão da conversa também exclui suas mensagens.
-- A restrição UNIQUE evita duplicar o mesmo envio por autor.
CREATE TABLE tb_mensagem (
                             cd_mensagem   NUMBER(10) PRIMARY KEY,
                             cd_conversa   NUMBER(10) NOT NULL
                  REFERENCES tb_conversa(cd_conversa) ON DELETE CASCADE,
                             ds_autor      VARCHAR2(10) NOT NULL
                  CHECK (ds_autor IN ('usuario', 'gaia')),
                             ds_texto      CLOB NOT NULL,
                             ds_requisicao VARCHAR2(36) NOT NULL,
                             dt_envio      TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
                             CONSTRAINT uq_mensagem_requisicao
                                 UNIQUE (cd_conversa, ds_requisicao, ds_autor)
);

CREATE SEQUENCE sq_tb_mensagem
    START WITH 1 INCREMENT BY 1 NOCACHE;

CREATE INDEX ix_mensagem_conversa
    ON tb_mensagem(cd_conversa);