-- Execute no mesmo schema da API, com a API parada.
-- Não execute scripts.sql novamente em um banco que já possui dados.
-- Oracle confirma DDL automaticamente. A coluna textual fica como respaldo.
-- Este bloco pode ser executado novamente caso a conversão seja interrompida.
declare
    tipo varchar2(30);
    existe_anterior number;
    invalidos number;
begin
    select count(*) into existe_anterior from user_tab_columns
        where table_name = 'TB_USUARIO' and column_name = 'DS_PERFIL_ANTERIOR';

    if existe_anterior = 0 then
        select data_type into tipo from user_tab_columns
            where table_name = 'TB_USUARIO' and column_name = 'DS_PERFIL';
        if tipo <> 'NUMBER' then
            execute immediate 'select count(*) from tb_usuario where ds_perfil not in (''ADMIN'',''USUARIO'') or ds_perfil is null' into invalidos;
            if invalidos > 0 then
                raise_application_error(-20001, 'Existem perfis desconhecidos. Conversão cancelada.');
            end if;
            execute immediate 'alter table tb_usuario rename column ds_perfil to ds_perfil_anterior';
        end if;
    end if;

    select count(*) into existe_anterior from user_tab_columns
        where table_name = 'TB_USUARIO' and column_name = 'DS_PERFIL';
    if existe_anterior = 0 then
        execute immediate 'alter table tb_usuario add (ds_perfil number(1))';
        execute immediate 'update tb_usuario set ds_perfil = case ds_perfil_anterior when ''ADMIN'' then 1 when ''USUARIO'' then 0 end';
        commit;
    else
        select count(*) into existe_anterior from user_tab_columns
            where table_name = 'TB_USUARIO' and column_name = 'DS_PERFIL_ANTERIOR';
        if existe_anterior > 0 then
            execute immediate 'update tb_usuario set ds_perfil = case ds_perfil_anterior when ''ADMIN'' then 1 when ''USUARIO'' then 0 end where ds_perfil is null';
            commit;
        end if;
    end if;

    select nullable into tipo from user_tab_columns
        where table_name = 'TB_USUARIO' and column_name = 'DS_PERFIL';
    if tipo = 'Y' then
        execute immediate 'alter table tb_usuario modify (ds_perfil default 0 not null)';
    end if;

    select count(*) into existe_anterior from user_constraints
        where constraint_name = 'CK_USUARIO_PERFIL_NUMERICO';
    if existe_anterior = 0 then
        execute immediate 'alter table tb_usuario add constraint ck_usuario_perfil_numerico check (ds_perfil in (0,1))';
    end if;
end;
/

-- Conferência sem expor senhas:
select ds_perfil, count(*) as quantidade from tb_usuario group by ds_perfil order by ds_perfil;
-- DS_PERFIL_ANTERIOR é apenas o respaldo da migração e não é usado pela API.
