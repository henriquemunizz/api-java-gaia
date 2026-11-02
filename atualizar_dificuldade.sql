-- Somente para quem já criou tb_missao usando o script anterior.
-- Não execute após o novo scripts.sql, que já contém a coluna.
alter table tb_missao add (
 tp_dificuldade number(1) default 1 not null
 constraint ck_missao_dificuldade check (tp_dificuldade in (1,2,3))
);
-- Missões existentes começam como fáceis; ajuste individualmente se necessário.
-- Remove o padrão para exigir preenchimento nos próximos INSERTs manuais.
alter table tb_missao modify (tp_dificuldade default null);
