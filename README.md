# Gaia API — Spring Boot, Maven, IntelliJ e Oracle

API REST da GAIA desenvolvida em **Java 21 e Spring Boot**, com banco de dados **Oracle** e integração com o **n8n** para as conversas com o avatar.

## Requisitos para executar localmente

* **JDK 21** instalado e configurado no IntelliJ IDEA.
* **IntelliJ IDEA** com suporte a projetos Maven.
* Acesso ao banco de dados **Oracle Cloud da FIAP**, com usuário, senha e URL JDBC válidos.
* Para utilizar o chat: workflow da GAIA **publicado no n8n** e credencial de autenticação do Webhook.

## Clonar e abrir o projeto

1. Clone o repositório:

```bash
   git clone https://github.com/henriquemunizz/api-java-gaia.git
   ```

2. Abra a pasta `api-java-gaia` no IntelliJ IDEA, importando o `pom.xml` como projeto **Maven**.
3. Aguarde o carregamento das dependências e confirme o **JDK 21** em `File > Project Structure > Project SDK`.
4. Para um banco Oracle novo, execute o `scripts.sql` manualmente no schema de destino. **Se as tabelas já existirem, não execute esse script novamente.**

## Configurar a execução do Spring Boot

1. No IntelliJ, abra **Run > Edit Configurations**.
2. Clique em **+ > Spring Boot** (ou **Application**, caso Spring Boot não apareça).
3. Configure a execução com os valores abaixo:

|Campo|Valor|
|-|-|
|Name|`GAIA API`|
|Main class|`br.com.fiap.api.ApiApplication`|
|JDK|`21`|
|Module / classpath|Módulo principal do projeto `api-java-gaia`|

4. Em **Environment variables** (use **Modify options > Environment variables** se necessário), configure:

```dotenv
   ORACLE_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:orcl
   ORACLE_USER=seu_usuario
   ORACLE_PASSWORD=sua_senha
   GAIA_FRONTEND_ORIGIN=http://localhost:5173
   COOKIE_SECURE=false
   ```

Substitua os valores de exemplo pelos dados reais do Oracle Cloud da FIAP. A URL deve ser a **URL JDBC** fornecida para o banco.

5. Clique em **Apply > OK**, selecione `GAIA API` no topo do IntelliJ e clique em **Run**.
6. Por padrão, a API inicia em **http://localhost:8080**.

> A configuração de execução do IntelliJ é **local**: não precisa ser enviada ao GitHub. Para executar no Render ou em outra hospedagem, cadastre as variáveis no painel do próprio serviço.

## Variáveis de ambiente

O arquivo `src/main/resources/application.properties` usa variáveis de ambiente para separar configurações e credenciais do código-fonte.

|Variável|Finalidade|Obrigatória para|
|-|-|-|
|`ORACLE_URL`|Endereço JDBC do banco Oracle|API e banco|
|`ORACLE_USER`|Usuário do banco Oracle|API e banco|
|`ORACLE_PASSWORD`|Senha do banco Oracle|API e banco|
|`GAIA_FRONTEND_ORIGIN`|Origens autorizadas do frontend React, separadas por vírgula (padrão: `http://localhost:5173`)|Integração com frontend|
|`PORT`|Porta HTTP utilizada pelo servidor (padrão: `8080`)|Opcional; hospedagem|
|`COOKIE_SECURE`|Exige HTTPS para envio de cookies quando `true` (padrão: `false`)|Cookies em produção|
|`COOKIE_SAME_SITE`|Política SameSite do cookie de sessão (padrão: `strict`)|Cookies entre sites|
|`GAIA_N8N_WEBHOOK_URL`|URL de produção do Webhook do n8n|Chat da GAIA|
|`GAIA_N8N_WEBHOOK_KEY`|Chave de autenticação utilizada na integração com o Webhook|Chat da GAIA|

As variáveis obrigatórias do Oracle devem estar disponíveis **antes** de iniciar o Spring Boot. Não grave senhas ou chaves diretamente no `application.properties` nem as envie ao repositório.

## Usar a API publicada no React local e na Vercel

No painel **Environment** do Render, configure as duas origens sem barra no final:

```dotenv
GAIA_FRONTEND_ORIGIN=https://gaia-react-gamma.vercel.app,http://localhost:5173
COOKIE_SAME_SITE=none
COOKIE_SECURE=true
```

Salve as variáveis e faça um novo deploy da API com a versão atualizada do filtro CORS.

No React local, crie `.env.local` na pasta do `package.json`:

```dotenv
VITE_API_URL=https://api-java-gaia.onrender.com
```

Reinicie `npm run dev`. Na Vercel, use a mesma `VITE_API_URL` e faça um novo deploy se alterar essa variável. As requisições devem manter `credentials: "include"`, pois a autenticação usa o cookie `JSESSIONID`. O navegador precisa permitir esse cookie entre sites.

## Configurar o chat da GAIA (`.env.chat`)

Para preparar a integração com o n8n, crie um arquivo chamado **`.env.chat` na raiz do projeto**, na mesma pasta do `pom.xml`:

```text
api-java-gaia/
├── src/
├── n8n/
├── .env.chat        <-- criar aqui
├── .gitignore
├── Dockerfile
├── pom.xml
└── README.md
```

Conteúdo do `.env.chat`:

```dotenv
GAIA_N8N_WEBHOOK_KEY=sua_chave_do_webhook

GAIA_N8N_WEBHOOK_URL=https://henriquemunizz.app.n8n.cloud/webhook/gaia-chat

```

* Use a **Production URL** do nó Webhook, e não a URL que contém `/webhook-test/`.
* A chave deve corresponder à autenticação exigida pelo n8n. O workflow precisa estar **Published**.
* **Atenção:** o Spring Boot não lê `.env.chat` automaticamente por padrão. Para garantir o funcionamento local, configure também `GAIA_N8N_WEBHOOK_URL` e `GAIA_N8N_WEBHOOK_KEY` em **Run > Edit Configurations > Environment variables**, com os mesmos valores do arquivo, a menos que haja um mecanismo específico de carregamento configurado.
* O `.env.chat` é ignorado pelo `.gitignore` e **não deve ser enviado ao GitHub**, pois pode conter uma chave secreta.
* Na hospedagem (por exemplo, Render), configure essas duas variáveis diretamente no painel **Environment**. O arquivo `.env.chat` é apenas para uso local.

## Testar a API

Com a API em execução, utilize o Postman ou outro cliente HTTP. Primeiro, faça o cadastro em `POST /usuarios` e o login em `POST /usuarios/login`, mantendo o cookie de sessão. Em seguida, teste as operações de missões, recompensas e chat. Para que o chat responda, o Webhook de produção precisa estar acessível e retornar JSON com o campo `resposta`.

O SQL não é executado automaticamente. Não existem credenciais do professor no projeto. Se já criou as tabelas antes da inclusão da dificuldade, execute atualizar_dificuldade.sql. Se o perfil ainda é textual (ADMIN/USUARIO), pare a API e execute atualizar_perfil.sql no mesmo schema antes de iniciar esta versão. Não execute scripts.sql novamente em um banco existente.

A migração converte ADMIN para 1 e USUARIO para 0, preservando IDs, nomes, senhas, progresso e históricos. A coluna ds_perfil passa a ser NUMBER(1), obrigatória, com padrão 0 e restrição para 0/1. A coluna ds_perfil_anterior conserva o texto anterior como respaldo da migração; não é usada pela API nem representa o perfil de novos cadastros. O bloco pode ser executado novamente após interrupção. DDL no Oracle confirma alterações automaticamente.

## Estrutura

* model: Usuario, Missao, Recompensa, UsuarioMissao, UsuarioRecompensa e UsuarioProgresso, com associações e getters/setters.
* dao: SQL e conversão de ResultSet para modelos.
* bo: UsuarioBo (normalização e senha) e UsuarioProgressoBo (conclusão, resgate e transações).
* controller: rotas, permissões e conversão para DTOs.
* factory: ConnectionFactory.
* config: autenticação por sessão, ModelMapper e CorsFilter.
* exception: respostas para validação, recurso inexistente, conflito e erro de banco.

## Campos e regras

|**Tabela**|**Entrada manual**|**Automático**|
|-|-|-|
|tb_usuario|nomeUsuario, senha, perfil (0 = usuário, 1 = administrador)|ID, hash de senha|
|tb_missao|nome, descricao, dificuldade, pontos, ativo|ID|
|tb_recompensa|nome, descricao, pontos (custo), ativo|ID|
|tb_usuario_missao|ação de concluir|ID, usuário, missão, pontos e data|
|tb_usuario_recompensa|ação de resgatar|ID, usuário, recompensa, custo e data|
|tb_usuario_progresso|nenhum|total ganho, saldo disponível, quantidade de conclusões e data|

Dificuldade: 1 = fácil, 2 = médio, 3 = difícil. Obrigatória e validada na API e no Oracle. O administrador define pontos separadamente. Ativo: 1 = ativo, 0 = inativo. A API bloqueia conclusão e resgate de itens inativos.

O mesmo usuário pode repetir a mesma missão: cada POST gera uma conclusão e soma pontos novamente, sem limite diário. O front deve bloquear duplo clique e não reenviar automaticamente essas ações. Conclusões e resgates gravam histórico e saldo na mesma transação. O bloqueio da linha de progresso evita gasto concorrente do mesmo saldo. A conclusão é declarada pelo usuário; não há comprovação externa. O total ganho não diminui em resgates; somente o saldo diminui. O custo/pontos no histórico é o valor da época, não muda ao editar o cadastro.

## Login e administração

Cadastro (POST /usuarios):

```json
{"nomeUsuario":"debora","senha":"UmaSenhaForte123","perfil":0}

```

Perfil é obrigatório: 0 = usuário, 1 = administrador. Conforme a regra deste projeto acadêmico, qualquer pessoa pode escolher administrador no cadastro e gerenciar missões e recompensas.

Login (POST /usuarios/login) usa apenas:

```json
{"nomeUsuario":"debora","senha":"UmaSenhaForte123"}

```

Nome: 3–40 caracteres (letras, números, ponto, hífen e sublinhado), normalizado para minúsculas. Senha: 8–128 caracteres, protegida com PBKDF2 e salt aleatório. Senhas e hashes nunca aparecem nos DTOs de resposta.

POST /usuarios/login cria uma sessão com cookie JSESSIONID. No front, use credentials: "include". A sessão expira após 30 minutos de inatividade. A conta é reconsultada a cada operação autenticada: exclusão da conta, troca de senha e mudança de perfil são verificadas mesmo em sessões antigas. Ao trocar sua própria senha, a sessão atual é mantida; outras sessões serão recusadas na próxima operação.

As respostas de usuário retornam perfil numérico (0/1). O login usa o perfil persistido no Oracle; informar um perfil no login não concede acesso. A edição de conta não altera o perfil. A API mantém a autorização das operações administrativas e reconsulta o usuário em cada requisição autenticada. Mantenha uma conta ADMIN se precisar continuar administrando o sistema; a exclusão da própria conta também é permitida a administradores.

## Endpoints

Todos os endpoints podem retornar 500 em falhas de banco. Nas rotas autenticadas, 401 significa sessão ausente, inválida ou expirada; 403 significa falta de permissão.

|**Método**|**Rota**|**Acesso**|**Respostas principais**|
|-|-|-|-|
|POST|/usuarios|Público, cadastro|201, 400, 409|
|POST|/usuarios/login|Público|200, 400, 401|
|POST|/usuarios/logout|Sessão atual|204|
|GET|/usuarios|ADMIN|200, 401, 403|
|GET|/usuarios/{id}|Próprio usuário ou ADMIN|200, 401, 403, 404|
|PUT|/usuarios/{id}|Próprio usuário ou ADMIN|200, 400, 401, 403, 404, 409|
|DELETE|/usuarios/{id}|Próprio usuário ou ADMIN|204, 401, 403, 404|
|GET|/usuarios/me|Próprio usuário|200, 401|
|PUT|/usuarios/me|Próprio usuário|200, 400, 401, 404, 409|
|DELETE|/usuarios/me|Próprio usuário|204, 401, 404|
|GET|/missoes|Logado|200, 401|
|GET|/missoes/{id}|Logado|200, 401, 404|
|POST|/missoes|ADMIN|201, 400, 401, 403|
|PUT|/missoes/{id}|ADMIN|200, 400, 401, 403, 404|
|DELETE|/missoes/{id}|ADMIN|204, 401, 403, 404, 409|
|GET|/recompensas|Logado|200, 401|
|GET|/recompensas/{id}|Logado|200, 401, 404|
|POST|/recompensas|ADMIN|201, 400, 401, 403|
|PUT|/recompensas/{id}|ADMIN|200, 400, 401, 403, 404|
|DELETE|/recompensas/{id}|ADMIN|204, 401, 403, 404, 409|
|GET|/usuarios/me/progresso|Próprio usuário|200, 401, 404|
|POST|/usuarios/me/missoes/{id}/concluir|Próprio usuário|200, 401, 404, 409|
|POST|/usuarios/me/recompensas/{id}/resgatar|Próprio usuário|200, 401, 404, 409|
|GET|/usuarios/me/missoes|Próprio usuário|200, 401|
|GET|/usuarios/me/recompensas|Próprio usuário|200, 401|

Identificador com formato inválido retorna 400. As listagens de missões e recompensas incluem itens inativos; a futura tela comum deve mostrar ativo=1.

## Editar usuários

PUT /usuarios/me ou PUT /usuarios/{id}:

```json
{"nomeUsuario":"debora_novo","senha":"OutraSenhaForte123"}

```

Para manter a senha atual, omita senha ou envie null. NomeUsuario continua obrigatório. Nome duplicado retorna 409. Administrador pode listar, consultar, editar e excluir contas. Usuário comum só pode acessar a própria conta.

## Cadastrar e editar missões/recompensas

POST /missoes ou PUT /missoes/{id}:

```json
{"nome":"Separar recicláveis","descricao":"Separe os resíduos de hoje.","pontos":20,"ativo":1,"dificuldade":1}

```

Para recompensa, use /recompensas e omita dificuldade. Pontos representa o custo do resgate.

## Exclusão real

DELETE agora executa DELETE FROM no Oracle.

* Missão ou recompensa sem histórico: exclui e retorna 204. Nova consulta retorna 404.
* Missão ou recompensa com histórico: retorna 409 e mantém os dados. A chave estrangeira impede apagar um item usado. Para retirá-lo de circulação, use PUT com ativo=0.
* Usuário: exclui a conta e seus registros de missão, recompensa e progresso em uma transação. Não exclui o catálogo de missões/recompensas nem os dados de outros usuários.
* Se a conta excluída for a conta da sessão atual, faz logout. Outras sessões dessa conta serão recusadas.
* ID inexistente: retorna 404 quando a pessoa tem permissão de consultar/gerenciar o recurso.

## Conversas com a GAIA

GET e POST em `/usuarios/me/conversas` listam e criam conversas. GET em `/usuarios/me/conversas/{id}/mensagens` consulta o histórico. POST na mesma rota recebe texto (até 2000 caracteres) e requisicaoId (UUID). A API envia ao n8n nome de usuário, progresso, catálogo ativo, pergunta e até 20 mensagens recentes. O webhook deve devolver um objeto JSON com o campo resposta. A API grava a pergunta e a resposta juntas após uma resposta válida.

