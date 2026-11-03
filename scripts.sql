-- Execute manualmente no schema Oracle de destino. A aplicação não executa este arquivo.
create table tb_usuario (
 cd_usuario number(10) primary key,
 nm_usuario varchar2(40) not null unique,
 ds_senha varchar2(200) not null,
 ds_perfil number(1) default 0 not null check (ds_perfil in (0,1))
);
create sequence sq_tb_usuario start with 1 increment by 1 nocache;

create table tb_usuario_progresso (
 cd_usuario number(10) primary key references tb_usuario(cd_usuario),
 nr_pontos_totais number(10) default 0 not null check (nr_pontos_totais >= 0),
 nr_saldo number(10) default 0 not null check (nr_saldo >= 0),
 nr_missoes number(10) default 0 not null check (nr_missoes >= 0),
 dt_atualizacao date default sysdate not null,
 constraint ck_saldo_total check (nr_saldo <= nr_pontos_totais)
);

create table tb_missao (
 cd_missao number(10) primary key,
 tp_dificuldade number(1) not null check (tp_dificuldade in (1,2,3)),
 nm_nome varchar2(100) not null,
 ds_descricao varchar2(500) not null,
 nr_pontos number(9) not null check (nr_pontos > 0),
 st_ativo number(1) default 1 not null check (st_ativo in (0,1))
);
create sequence sq_tb_missao start with 1 increment by 1 nocache;

-- Cada linha é uma conclusão/resgate. Não há unique(usuario,item): permite repetição.
create table tb_usuario_missao (
 cd_registro number(10) primary key,
 cd_usuario number(10) not null references tb_usuario(cd_usuario),
 cd_missao number(10) not null references tb_missao(cd_missao),
 nr_pontos number(9) not null check (nr_pontos > 0),
 dt_registro date default sysdate not null
);
create sequence sq_tb_usuario_missao start with 1 increment by 1 nocache;
create index ix_usuario_missao on tb_usuario_missao(cd_usuario);
create index ix_missao_historico on tb_usuario_missao(cd_missao);

create table tb_recompensa (
 cd_recompensa number(10) primary key,
 nm_nome varchar2(100) not null,
 ds_descricao varchar2(500) not null,
 nr_pontos number(9) not null check (nr_pontos > 0),
 st_ativo number(1) default 1 not null check (st_ativo in (0,1))
);
create sequence sq_tb_recompensa start with 1 increment by 1 nocache;

-- Cada linha é uma conclusão/resgate. Não há unique(usuario,item): permite repetição.
create table tb_usuario_recompensa (
 cd_registro number(10) primary key,
 cd_usuario number(10) not null references tb_usuario(cd_usuario),
 cd_recompensa number(10) not null references tb_recompensa(cd_recompensa),
 nr_pontos number(9) not null check (nr_pontos > 0),
 dt_registro date default sysdate not null
);
create sequence sq_tb_usuario_recompensa start with 1 increment by 1 nocache;
create index ix_usuario_recompensa on tb_usuario_recompensa(cd_usuario);
create index ix_recompensa_historico on tb_usuario_recompensa(cd_recompensa);

-- Exemplos opcionais:
insert into tb_missao (cd_missao,nm_nome,ds_descricao,nr_pontos,st_ativo,tp_dificuldade) values (sq_tb_missao.nextval,'Separar recicláveis','Separe os resíduos recicláveis de hoje.',20,1,1);
insert into tb_recompensa values (sq_tb_recompensa.nextval,'Selo Gaia','Resgate seu selo de participação.',50,1);
commit;
-- No cadastro pela API, envie perfil=0 (usuário) ou perfil=1 (administrador).
-- Para um schema já existente com perfil textual, execute atualizar_perfil.sql.

