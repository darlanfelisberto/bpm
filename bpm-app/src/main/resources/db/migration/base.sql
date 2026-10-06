-- avaliacao
-- .
-- instrumento_avaliativo
-- , onte teremos a auto avaliacao 2025, auto avaliacao 2026, Docente Peslo didente 2026/1,Docente Pelo didente 2026/2
-- avaliacao.instrumento_sessao()
-- avaliacao.sessao_questao()
-- avaliacao.tipo_sessao(), pagina(as questoes são renderizadas em uma pagina, ou proxima pagina, sem misturar com outras sessao ou pagina) e sessao(é apenas uma gorma de abrupar questao e resposta)
--
-- avaliacao.questao()
-- avaliacao.tipo_questao() --alternativa, descritiva
-- avaliacao.opcao_questao()
--
-- avaliacao.resposta_instrumento
-- avaliacao.resposta_questao()
--
-- sig.componente_curricular
--
-- auth.usuario
-- auth.pessoa


                                -- =====================================================================
-- Schema: avaliacao  (PostgreSQL 13+; gen_random_uuid() nativo)
-- Convenções: PK = <tabela>_id (uuid); FK com o mesmo nome da PK referenciada
--             (auto-relacionamentos usam sufixo/prefixo: *_pai_id, *_origem_id)
-- Núcleo genérico reutilizado pelos módulos AUTOAVALIACAO, ADPD, APRENDIZAGEM
-- =====================================================================
create schema if not exists avaliacao;

-- ---------------------------------------------------------------------
-- 1. Instrumento (uma aplicação/ciclo: autoavaliação 2025, ADPD 2026/1...)
-- ---------------------------------------------------------------------
create table avaliacao.instrumento_avaliativo
(
    instrumento_avaliativo_id        uuid primary key      default gen_random_uuid(),
    instrumento_avaliativo_origem_id uuid references avaliacao.instrumento_avaliativo (instrumento_avaliativo_id),
    modulo                           varchar(20)  not null
        check (modulo in ('AUTOAVALIACAO', 'ADPD', 'APRENDIZAGEM')),
    titulo                           varchar(200) not null,
    descricao                        text,
    ciclo_referencia                 varchar(20)  not null,               -- '2025', '2026/1'
    inicio_aplicacao                 timestamptz  not null,               -- RF04
    fim_aplicacao                    timestamptz  not null,
    anonimo                          boolean      not null default false, -- RF08 / RNF03
    permite_edicao                   boolean      not null default true,  -- RF07
    status                           varchar(12)  not null default 'RASCUNHO' check (status in ('RASCUNHO', 'PUBLICADO', 'ENCERRADO', 'CONSOLIDADO')),
    consolidado_em                   timestamptz,
    criado_por                       varchar(60)  not null,
    criado_em                        timestamptz  not null default now(),
    atualizado_em                    timestamptz  not null default now(),
    check (fim_aplicacao > inicio_aplicacao),
    check ((status = 'CONSOLIDADO') = (consolidado_em is not null)),
    unique (modulo, ciclo_referencia, titulo)
);

-- ---------------------------------------------------------------------
-- 2. Público-alvo (RF05)
-- ---------------------------------------------------------------------
create table avaliacao.publico_alvo
(
    publico_alvo_id           uuid primary key default gen_random_uuid(),
    instrumento_avaliativo_id uuid        not null references avaliacao.instrumento_avaliativo (instrumento_avaliativo_id),
    escopo                    varchar(12) not null check (escopo in ('INDIVIDUO', 'TURMA', 'CURSO', 'CAMPUS', 'INSTITUICAO')),
    referencia_externa        varchar(60), -- id no SIGAA
    check ((escopo = 'INSTITUICAO') = (referencia_externa is null))
);
create unique index ux_publico_alvo on avaliacao.publico_alvo (instrumento_avaliativo_id, escopo, coalesce(referencia_externa, ''));

-- ---------------------------------------------------------------------
-- 3. Estrutura do instrumento: página > sessão (auto-relacionamento)
--    PAGINA  = unidade de renderização (nunca tem pai)
--    SESSAO  = agrupador de questões/respostas dentro de uma página
-- ---------------------------------------------------------------------
create table avaliacao.instrumento_sessao
(
    instrumento_sessao_id     uuid primary key default gen_random_uuid(),
    instrumento_avaliativo_id uuid         not null references avaliacao.instrumento_avaliativo (instrumento_avaliativo_id),
    instrumento_sessao_pai_id uuid,
    tipo                      varchar(6)   not null check (tipo in ('PAGINA', 'SESSAO')),
    titulo                    varchar(200) not null,
    descricao                 text,
    ordem                     smallint     not null,
    unique (instrumento_avaliativo_id, instrumento_sessao_id),
    foreign key (instrumento_avaliativo_id, instrumento_sessao_pai_id) references avaliacao.instrumento_sessao (instrumento_avaliativo_id, instrumento_sessao_id),
    check ((tipo = 'PAGINA') = (instrumento_sessao_pai_id is null))
);

-- ---------------------------------------------------------------------
-- 4. Conjuntos de opções reutilizáveis (ex.: escala Likert de 5 pontos)
-- ---------------------------------------------------------------------
create table avaliacao.conjunto_opcao
(
    conjunto_opcao_id uuid primary key default gen_random_uuid(),
    nome              varchar(100) not null unique
);

create table avaliacao.opcao_questao
(
    opcao_questao_id  uuid primary key      default gen_random_uuid(),
    conjunto_opcao_id uuid         not null references avaliacao.conjunto_opcao (conjunto_opcao_id),
    texto             varchar(300) not null,
    valor             numeric(6, 2),                       -- peso numérico p/ relatórios (RF09)
    correta           boolean      not null default false, -- módulo APRENDIZAGEM
    ordem             smallint     not null
);

-- ---------------------------------------------------------------------
-- 5. Questão (catálogo; pode ser reutilizada em vários instrumentos)
-- ---------------------------------------------------------------------
create table avaliacao.questao
(
    questao_id        uuid primary key     default gen_random_uuid(),
    conjunto_opcao_id uuid references avaliacao.conjunto_opcao (conjunto_opcao_id),
    enunciado         text        not null,
    tipo              varchar(16) not null check (tipo in ('ESCOLHA_UNICA', 'MULTIPLA_ESCOLHA', 'ESCALA', 'DESCRITIVA')),
    assunto           varchar(100), -- RF01
    criado_por        varchar(60) not null,
    criado_em         timestamptz not null default now(),
    check ((tipo = 'DESCRITIVA') = (conjunto_opcao_id is null))
);

-- Posicionamento da questão no instrumento
create table avaliacao.sessao_questao
(
    sessao_questao_id     uuid primary key  default gen_random_uuid(),
    instrumento_sessao_id uuid     not null references avaliacao.instrumento_sessao (instrumento_sessao_id),
    questao_id            uuid     not null references avaliacao.questao (questao_id),
    ordem                 smallint not null,
    obrigatoria           boolean  not null default false, -- RF03
    peso                  numeric(6, 2),                   -- módulo APRENDIZAGEM
    unique (instrumento_sessao_id, questao_id)
);

-- ---------------------------------------------------------------------
-- 6. Participação: quem já respondeu (SEM vínculo com a resposta)
-- ---------------------------------------------------------------------
create table avaliacao.participacao
(
    participacao_id           uuid primary key     default gen_random_uuid(),
    instrumento_avaliativo_id uuid        not null references avaliacao.instrumento_avaliativo (instrumento_avaliativo_id),
    usuario_ref               varchar(60) not null, -- matrícula/login SIGAA
    alvo_ref                  varchar(60),          -- turma avaliada (ADPD)
    registrada_em             timestamptz not null default now()
);
create unique index ux_participacao on avaliacao.participacao (instrumento_avaliativo_id, usuario_ref, coalesce(alvo_ref, ''));

-- ---------------------------------------------------------------------
-- 7. Respostas
-- ---------------------------------------------------------------------
create table avaliacao.resposta_instrumento
(
    resposta_instrumento_id   uuid primary key     default gen_random_uuid(),
    instrumento_avaliativo_id uuid        not null references avaliacao.instrumento_avaliativo (instrumento_avaliativo_id),
    alvo_ref                  varchar(60),                                -- turma/docente avaliado (ADPD)
    usuario_ref               varchar(60),                                -- nulo ao enviar, se anônimo
    status                    varchar(12) not null default 'EM_ANDAMENTO'  check (status in ('EM_ANDAMENTO', 'ENVIADA')), -- RF06
    data_envio                date,                                       -- só a data, p/ dificultar correlação
    check ((status = 'ENVIADA') = (data_envio is not null))
);

create table avaliacao.resposta_questao
(
    resposta_questao_id     uuid primary key default gen_random_uuid(),
    resposta_instrumento_id uuid not null references avaliacao.resposta_instrumento (resposta_instrumento_id),
    sessao_questao_id       uuid not null references avaliacao.sessao_questao (sessao_questao_id),
    opcao_questao_id        uuid references avaliacao.opcao_questao (opcao_questao_id),
    texto                   text,
    check (num_nonnulls(opcao_questao_id, texto) = 1)
);
-- múltipla escolha: uma linha por opção marcada, sem repetir
create unique index ux_resp_questao_opcao
    on avaliacao.resposta_questao (resposta_instrumento_id, sessao_questao_id, opcao_questao_id) where opcao_questao_id is not null;
-- descritiva: uma linha por questão
create unique index ux_resp_questao_texto
    on avaliacao.resposta_questao (resposta_instrumento_id, sessao_questao_id) where opcao_questao_id is null;

-- ---------------------------------------------------------------------
-- 8. Encaminhamento (RF14): nenhum ciclo é consolidado sem registro
-- ---------------------------------------------------------------------
create table avaliacao.encaminhamento
(
    encaminhamento_id         uuid primary key     default gen_random_uuid(),
    instrumento_avaliativo_id uuid        not null references avaliacao.instrumento_avaliativo (instrumento_avaliativo_id),
    alvo_ref                  varchar(60), -- ex.: docente/turma (RF-AD08)
    tipo                      varchar(22) not null check (tipo in ('DECISAO', 'PLANO_ACAO', 'JUSTIFICATIVA_NAO_ACAO')),
    descricao                 text        not null,
    responsavel_ref           varchar(60) not null,
    prazo                     date,
    registrado_em             timestamptz not null default now()
);

-- ---------------------------------------------------------------------
-- 10. Índices de apoio (FKs consultadas com frequência)
-- ---------------------------------------------------------------------
create index ix_instrumento_sessao_instrumento
    on avaliacao.instrumento_sessao (instrumento_avaliativo_id, instrumento_sessao_pai_id, ordem);
create index ix_sessao_questao_questao
    on avaliacao.sessao_questao (questao_id);
create index ix_questao_conjunto
    on avaliacao.questao (conjunto_opcao_id);
create index ix_resposta_instrumento_instrumento
    on avaliacao.resposta_instrumento (instrumento_avaliativo_id, alvo_ref);
create index ix_resposta_questao_sessao_questao
    on avaliacao.resposta_questao (sessao_questao_id);
create index ix_encaminhamento_instrumento
    on avaliacao.encaminhamento (instrumento_avaliativo_id, alvo_ref);