# Gaia API — Maven, IntelliJ e Oracle

## Referências do professor
- Apostila16: Spring Boot, controllers REST, DTOs, ModelMapper, validação, DAOs com JDBC, DataSource, PreparedStatement e SQL separado.
- Apostila13: ConnectionFactory, consulta por ID, listagem, atualização e exclusão física com DELETE, verificando o número de linhas alteradas.
- ConnectionFactory foi adaptada ao DataSource da Apostila16, mantendo as credenciais em variáveis de ambiente. Cada operação fecha sua conexão; não há uma única conexão compartilhada entre requisições.
- As apostilas consultadas não possuem BO. Foram acrescentadas duas BOs simples para concentrar as regras de usuários e pontos exigidas na avaliação. Sem JPA, Lombok, camada service ou mudança de framework.

## Abrir e executar
1. Abra o pom.xml no IntelliJ como projeto Maven e carregue as dependências.
2. Use JDK 21. O pom mantém as versões da Apostila16.
3. Execute scripts.sql manualmente em um schema Oracle novo.
4. Em Run > Edit Configurations > ApiApplication > Environment variables, configure:
   - ORACLE_URL: jdbc:oracle:thin:@//HOST:1521/SERVICE_NAME ou a URL por SID do seu servidor.
   - ORACLE_USER: usuário do banco.
   - ORACLE_PASSWORD: senha do banco.
   - GAIA_FRONTEND_ORIGIN: origem exata do front. Padrão: http://localhost:5173.
   - COOKIE_SECURE: padrão false no desenvolvimento; true com HTTPS.
5. Execute ApiApplication. Endereço padrão: http://localhost:8080.

O SQL não é executado automaticamente. Não existem credenciais do professor no projeto.
Se já criou as tabelas antes da inclusão da dificuldade, execute apenas atualizar_dificuldade.sql. As mudanças de gerenciamento de usuários e exclusão desta revisão não exigem novas alterações no banco.

## Estrutura
- model: Usuario, Missao, Recompensa, UsuarioMissao, UsuarioRecompensa e UsuarioProgresso, com associações e getters/setters.
- dao: SQL e conversão de ResultSet para modelos.
- bo: UsuarioBo (normalização e senha) e UsuarioProgressoBo (conclusão, resgate e transações).
- controller: rotas, permissões e conversão para DTOs.
- factory: ConnectionFactory.
- config: autenticação por sessão, ModelMapper e CorsFilter.
- exception: respostas para validação, recurso inexistente, conflito e erro de banco.

## Campos e regras
| Tabela | Entrada manual | Automático |
|---|---|---|
| tb_usuario | nomeUsuario e senha | ID, hash de senha, perfil USUARIO |
| tb_missao | nome, descricao, dificuldade, pontos, ativo | ID |
| tb_recompensa | nome, descricao, pontos (custo), ativo | ID |
| tb_usuario_missao | ação de concluir | ID, usuário, missão, pontos e data |
| tb_usuario_recompensa | ação de resgatar | ID, usuário, recompensa, custo e data |
| tb_usuario_progresso | nenhum | total ganho, saldo disponível, quantidade de conclusões e data |

Dificuldade: 1 = fácil, 2 = médio, 3 = difícil. Obrigatória e validada na API e no Oracle. O administrador define pontos separadamente.
Ativo: 1 = ativo, 0 = inativo. A API bloqueia conclusão e resgate de itens inativos.

O mesmo usuário pode repetir a mesma missão: cada POST gera uma conclusão e soma pontos novamente, sem limite diário. O front deve bloquear duplo clique e não reenviar automaticamente essas ações.
Conclusões e resgates gravam histórico e saldo na mesma transação. O bloqueio da linha de progresso evita gasto concorrente do mesmo saldo. A conclusão é declarada pelo usuário; não há comprovação externa.
O total ganho não diminui em resgates; somente o saldo diminui. O custo/pontos no histórico é o valor da época, não muda ao editar o cadastro.

## Login e administração
Cadastro e login usam:
```json
{"nomeUsuario":"debora","senha":"UmaSenhaForte123"}
```
Nome: 3–40 caracteres (letras, números, ponto, hífen e sublinhado), normalizado para minúsculas.
Senha: 8–128 caracteres, protegida com PBKDF2 e salt aleatório. Senhas e hashes nunca aparecem nos DTOs de resposta.

POST /usuarios/login cria uma sessão com cookie JSESSIONID. No front, use credentials: "include". A sessão expira após 30 minutos de inatividade.
A conta é reconsultada a cada operação autenticada: exclusão da conta, troca de senha e mudança de perfil são verificadas mesmo em sessões antigas.
Ao trocar sua própria senha, a sessão atual é mantida; outras sessões serão recusadas na próxima operação.

O cadastro sempre cria USUARIO. Nem o cadastro nem a edição aceitam alterar perfil. Para criar o primeiro administrador, cadastre uma conta e execute:
```sql
update tb_usuario set ds_perfil='ADMIN' where nm_usuario='debora';
commit;
```
O perfil atualizado é lido na próxima operação autenticada.
Mantenha uma conta ADMIN se precisar continuar administrando o sistema; a exclusão da própria conta também é permitida a administradores.

## Endpoints
Todos os endpoints podem retornar 500 em falhas de banco. Nas rotas autenticadas, 401 significa sessão ausente, inválida ou expirada; 403 significa falta de permissão.

| Método | Rota | Acesso | Respostas principais |
|---|---|---|---|
| POST | /usuarios | Público, cadastro | 201, 400, 409 |
| POST | /usuarios/login | Público | 200, 400, 401 |
| POST | /usuarios/logout | Sessão atual | 204 |
| GET | /usuarios | ADMIN | 200, 401, 403 |
| GET | /usuarios/{id} | Próprio usuário ou ADMIN | 200, 401, 403, 404 |
| PUT | /usuarios/{id} | Próprio usuário ou ADMIN | 200, 400, 401, 403, 404, 409 |
| DELETE | /usuarios/{id} | Próprio usuário ou ADMIN | 204, 401, 403, 404 |
| GET | /usuarios/me | Próprio usuário | 200, 401 |
| PUT | /usuarios/me | Próprio usuário | 200, 400, 401, 404, 409 |
| DELETE | /usuarios/me | Próprio usuário | 204, 401, 404 |
| GET | /missoes | Logado | 200, 401 |
| GET | /missoes/{id} | Logado | 200, 401, 404 |
| POST | /missoes | ADMIN | 201, 400, 401, 403 |
| PUT | /missoes/{id} | ADMIN | 200, 400, 401, 403, 404 |
| DELETE | /missoes/{id} | ADMIN | 204, 401, 403, 404, 409 |
| GET | /recompensas | Logado | 200, 401 |
| GET | /recompensas/{id} | Logado | 200, 401, 404 |
| POST | /recompensas | ADMIN | 201, 400, 401, 403 |
| PUT | /recompensas/{id} | ADMIN | 200, 400, 401, 403, 404 |
| DELETE | /recompensas/{id} | ADMIN | 204, 401, 403, 404, 409 |
| GET | /usuarios/me/progresso | Próprio usuário | 200, 401, 404 |
| POST | /usuarios/me/missoes/{id}/concluir | Próprio usuário | 200, 401, 404, 409 |
| POST | /usuarios/me/recompensas/{id}/resgatar | Próprio usuário | 200, 401, 404, 409 |
| GET | /usuarios/me/missoes | Próprio usuário | 200, 401 |
| GET | /usuarios/me/recompensas | Próprio usuário | 200, 401 |

Identificador com formato inválido retorna 400. As listagens de missões e recompensas incluem itens inativos; a futura tela comum deve mostrar ativo=1.

## Editar usuários
PUT /usuarios/me ou PUT /usuarios/{id}:
```json
{"nomeUsuario":"debora_novo","senha":"OutraSenhaForte123"}
```
Para manter a senha atual, omita senha ou envie null. NomeUsuario continua obrigatório. Nome duplicado retorna 409.
Administrador pode listar, consultar, editar e excluir contas. Usuário comum só pode acessar a própria conta.

## Cadastrar e editar missões/recompensas
POST /missoes ou PUT /missoes/{id}:
```json
{"nome":"Separar recicláveis","descricao":"Separe os resíduos de hoje.","pontos":20,"ativo":1,"dificuldade":1}
```
Para recompensa, use /recompensas e omita dificuldade. Pontos representa o custo do resgate.

## Exclusão real
DELETE agora executa DELETE FROM no Oracle, como na Apostila13.
- Missão ou recompensa sem histórico: exclui e retorna 204. Nova consulta retorna 404.
- Missão ou recompensa com histórico: retorna 409 e mantém os dados. A chave estrangeira impede apagar um item usado. Para retirá-lo de circulação, use PUT com ativo=0.
- Usuário: exclui a conta e seus registros de missão, recompensa e progresso em uma transação. Não exclui o catálogo de missões/recompensas nem os dados de outros usuários.
- Se a conta excluída for a conta da sessão atual, faz logout. Outras sessões dessa conta serão recusadas.
- ID inexistente: retorna 404 quando a pessoa tem permissão de consultar/gerenciar o recurso.

## Próximas etapas sob responsabilidade da equipe
Validar rotas no Oracle, integrar o front, produzir MER e diagrama de classes compatíveis, PDF com prints reais, publicar no GitHub e fazer deploy.
Preservar Home, Integrantes, FAQ, Sobre e Contato. A aba Solução terá Missões, Recompensas, login e controles de administrador.
Na hospedagem, a configuração de domínio/cookie deve ser revisada conforme os endereços escolhidos: o padrão SameSite=Strict foi preparado para desenvolvimento no mesmo site (localhost), não para front e API em sites independentes.

## Verificação desta revisão
Os vínculos entre modelos, DAOs, controllers e SQL foram revisados. Foram incluídos testes locais de senha, permissões, sessão após exclusão/troca de senha e regras de pontos/transação. Não foi possível concluir a compilação nem executar esses testes neste ambiente: o compilador não conseguiu carregar as dependências locais. A compilação no IntelliJ/Maven e a validação real das rotas no Oracle continuam pendentes. Nenhuma tabela foi criada ou alterada no seu servidor.
