-- Execute uma vez no mesmo schema da API. Não recria nem apaga tabelas existentes.
-- Oracle confirma DDL automaticamente. Confira USER_TABLES antes de executar.
create table tb_conversa (
 cd_conversa number(10) primary key,
 cd_usuario number(10) not null references tb_usuario(cd_usuario) on delete cascade,
 nm_titulo varchar2(80 char) default 'Nova conversa' not null,
 dt_criacao timestamp default systimestamp not null
);
create sequence sq_tb_conversa start with 1 increment by 1 nocache;
create index ix_conversa_usuario on tb_conversa(cd_usuario);

create table tb_mensagem (
 cd_mensagem number(10) primary key,
 cd_conversa number(10) not null references tb_conversa(cd_conversa) on delete cascade,
 ds_autor varchar2(10) not null check (ds_autor in ('usuario','gaia')),
 ds_texto clob not null,
 ds_requisicao varchar2(36) not null,
 dt_envio timestamp default systimestamp not null,
 constraint uq_mensagem_requisicao unique(cd_conversa,ds_requisicao,ds_autor)
);
create sequence sq_tb_mensagem start with 1 increment by 1 nocache;
create index ix_mensagem_conversa on tb_mensagem(cd_conversa);
