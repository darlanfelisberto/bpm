-- =====================================================================
-- Migration V6: Reestruturacao do nucleo de avaliacao conforme base.sql
-- Substitui o prototipo inicial (V4/V5) pelo modelo com PKs UUID,
-- sessoes hierarquicas, conjunto reutilizavel de opcoes,
-- participacao dissociada e encaminhamentos compulsorios (RF14).
-- =====================================================================

DROP SCHEMA IF EXISTS avaliacao CASCADE;
CREATE SCHEMA avaliacao;

-- ---------------------------------------------------------------------
-- 1. Instrumento (uma aplicacao/ciclo: autoavaliacao 2025, ADPD 2026/1...)
-- ---------------------------------------------------------------------
create table avaliacao.tipo_status(
                                      tipo_status_id   VARCHAR(12) PRIMARY KEY NOT NULL CHECK (tipo_status_id IN ('RASCUNHO', 'PUBLICADO', 'ENCERRADO', 'CONSOLIDADO')),
                                      descricao        VARCHAR(100) NOT NULL
);

INSERT INTO avaliacao.tipo_status (tipo_status_id, descricao)VALUES
    ('RASCUNHO', 'RASCUNHO'),
    ('PUBLICADO', 'PUBLICADO'),
    ('ENCERRADO', 'ENCERRADO'),
    ('CONSOLIDADO', 'CONSOLIDADO');

CREATE TABLE avaliacao.instrumento_avaliativo
(
    instrumento_avaliativo_id        UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    instrumento_avaliativo_origem_id UUID REFERENCES avaliacao.instrumento_avaliativo (instrumento_avaliativo_id),
    titulo                           VARCHAR(200) NOT NULL,
    descricao                        TEXT,
    ciclo_referencia                 VARCHAR(20),               -- '2025', '2026/1'
    inicio_aplicacao                 TIMESTAMP  NOT NULL,               -- RF04
    fim_aplicacao                    TIMESTAMP  NOT NULL,
    anonimo                          BOOLEAN      NOT NULL DEFAULT FALSE, -- RF08 / RNF03
    permite_edicao                   BOOLEAN      NOT NULL DEFAULT TRUE,  -- RF07
    tipo_status_id                           VARCHAR(12)  NOT NULL references avaliacao.tipo_status (tipo_status_id),
    consolidado_em                   TIMESTAMP,
    criado_por                       VARCHAR(60)  NOT NULL,
    criado_em                        TIMESTAMP  NOT NULL DEFAULT now(),
    atualizado_em                    TIMESTAMP  NOT NULL DEFAULT now(),
    CHECK (fim_aplicacao > inicio_aplicacao),
    CHECK ((status = 'CONSOLIDADO') = (consolidado_em IS NOT NULL)),
    UNIQUE (ciclo_referencia, titulo)
);

INSERT INTO avaliacao.instrumento_avaliativo
(instrumento_avaliativo_id, instrumento_avaliativo_origem_id, titulo, descricao, ciclo_referencia, inicio_aplicacao, fim_aplicacao, anonimo, permite_edicao, tipo_status_id, consolidado_em, criado_por, criado_em, atualizado_em)
VALUES(gen_random_uuid(), null, 'Autoavaliacao', 'descrisao', '2026', '2026-08-08 00:00:00', '2026-08-09 00:00:00', false, true, 'RASCUNHO', null, '2026-08-08 00:00:00', now(), now());



-- ---------------------------------------------------------------------
-- 2. Publico-alvo (RF05)
-- ---------------------------------------------------------------------
CREATE TABLE avaliacao.publico_alvo
(
    publico_alvo_id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instrumento_avaliativo_id UUID        NOT NULL REFERENCES avaliacao.instrumento_avaliativo (instrumento_avaliativo_id),
    escopo                    VARCHAR(12) NOT NULL CHECK (escopo IN ('INDIVIDUO', 'TURMA', 'CURSO', 'CAMPUS', 'INSTITUICAO')),
    referencia_externa        VARCHAR(60), -- id no SIGAA
    CHECK ((escopo = 'INSTITUICAO') = (referencia_externa IS NULL))
);
CREATE UNIQUE INDEX ux_publico_alvo ON avaliacao.publico_alvo (instrumento_avaliativo_id, escopo, coalesce(referencia_externa, ''));

-- ---------------------------------------------------------------------
-- 3. Estrutura do instrumento: pagina > sessao (auto-relacionamento)
--    PAGINA  = unidade de renderizacao (nunca tem pai)
--    SESSAO  = agrupador de questoes/respostas dentro de uma pagina
-- ---------------------------------------------------------------------
CREATE TABLE avaliacao.instrumento_sessao
(
    instrumento_sessao_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    instrumento_avaliativo_id UUID         NOT NULL REFERENCES avaliacao.instrumento_avaliativo (instrumento_avaliativo_id),
    instrumento_sessao_pai_id UUID,
    tipo                      VARCHAR(6)   NOT NULL CHECK (tipo IN ('PAGINA', 'SESSAO')),
    titulo                    VARCHAR(200) NOT NULL,
    descricao                 TEXT,
    ordem                     SMALLINT     NOT NULL,
    UNIQUE (instrumento_avaliativo_id, instrumento_sessao_id),
    FOREIGN KEY (instrumento_avaliativo_id, instrumento_sessao_pai_id) REFERENCES avaliacao.instrumento_sessao (instrumento_avaliativo_id, instrumento_sessao_id),
    CHECK ((tipo = 'PAGINA') = (instrumento_sessao_pai_id IS NULL))
);

-- ---------------------------------------------------------------------
-- 4. Conjuntos de opcoes reutilizaveis (ex.: escala Likert de 5 pontos)
-- ---------------------------------------------------------------------
CREATE TABLE avaliacao.conjunto_opcao
(
    conjunto_opcao_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome              VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE avaliacao.opcao_questao
(
    opcao_questao_id  UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    conjunto_opcao_id UUID         NOT NULL REFERENCES avaliacao.conjunto_opcao (conjunto_opcao_id),
    texto             VARCHAR(300) NOT NULL,
    valor             NUMERIC(6, 2),                       -- peso numerico p/ relatorios (RF09)
    correta           BOOLEAN      NOT NULL DEFAULT FALSE, -- modulo APRENDIZAGEM
    ordem             SMALLINT     NOT NULL
);

-- ---------------------------------------------------------------------
-- 5. Questao (catalogo; pode ser reutilizada em varios instrumentos)
-- ---------------------------------------------------------------------
CREATE TABLE avaliacao.questao
(
    questao_id        UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    conjunto_opcao_id UUID REFERENCES avaliacao.conjunto_opcao (conjunto_opcao_id),
    enunciado         TEXT        NOT NULL,
    tipo              VARCHAR(16) NOT NULL CHECK (tipo IN ('ESCOLHA_UNICA', 'MULTIPLA_ESCOLHA', 'ESCALA', 'DESCRITIVA')),
    assunto           VARCHAR(100), -- RF01
    criado_por        VARCHAR(60) NOT NULL,
    criado_em         TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK ((tipo = 'DESCRITIVA') = (conjunto_opcao_id IS NULL))
);

-- Posicionamento da questao no instrumento
CREATE TABLE avaliacao.sessao_questao
(
    sessao_questao_id     UUID PRIMARY KEY  DEFAULT gen_random_uuid(),
    instrumento_sessao_id UUID     NOT NULL REFERENCES avaliacao.instrumento_sessao (instrumento_sessao_id),
    questao_id            UUID     NOT NULL REFERENCES avaliacao.questao (questao_id),
    ordem                 SMALLINT NOT NULL,
    obrigatoria           BOOLEAN  NOT NULL DEFAULT FALSE, -- RF03
    peso                  NUMERIC(6, 2),                   -- modulo APRENDIZAGEM
    UNIQUE (instrumento_sessao_id, questao_id)
);

-- ---------------------------------------------------------------------
-- 6. Participacao: quem ja respondeu (SEM vinculo com a resposta)
-- ---------------------------------------------------------------------
CREATE TABLE avaliacao.participacao
(
    participacao_id           UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    instrumento_avaliativo_id UUID        NOT NULL REFERENCES avaliacao.instrumento_avaliativo (instrumento_avaliativo_id),
    usuario_ref               VARCHAR(60) NOT NULL, -- matricula/login SIGAA
    alvo_ref                  VARCHAR(60),          -- turma avaliada (ADPD)
    registrada_em             TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX ux_participacao ON avaliacao.participacao (instrumento_avaliativo_id, usuario_ref, coalesce(alvo_ref, ''));

-- ---------------------------------------------------------------------
-- 7. Respostas
-- ---------------------------------------------------------------------
CREATE TABLE avaliacao.resposta_instrumento
(
    resposta_instrumento_id   UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    instrumento_avaliativo_id UUID        NOT NULL REFERENCES avaliacao.instrumento_avaliativo (instrumento_avaliativo_id),
    alvo_ref                  VARCHAR(60),                                -- turma/docente avaliado (ADPD)
    usuario_ref               VARCHAR(60),                                -- nulo ao enviar, se anonimo
    status                    VARCHAR(12) NOT NULL DEFAULT 'EM_ANDAMENTO'  CHECK (status IN ('EM_ANDAMENTO', 'ENVIADA')), -- RF06
    data_envio                DATE,                                       -- so a data, p/ dificultar correlacao
    CHECK ((status = 'ENVIADA') = (data_envio IS NOT NULL))
);

CREATE TABLE avaliacao.resposta_questao
(
    resposta_questao_id     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resposta_instrumento_id UUID NOT NULL REFERENCES avaliacao.resposta_instrumento (resposta_instrumento_id),
    sessao_questao_id       UUID NOT NULL REFERENCES avaliacao.sessao_questao (sessao_questao_id),
    opcao_questao_id        UUID REFERENCES avaliacao.opcao_questao (opcao_questao_id),
    texto                   TEXT,
    CHECK (num_nonnulls(opcao_questao_id, texto) = 1)
);
-- multipla escolha: uma linha por opcao marcada, sem repetir
CREATE UNIQUE INDEX ux_resp_questao_opcao
    ON avaliacao.resposta_questao (resposta_instrumento_id, sessao_questao_id, opcao_questao_id) WHERE opcao_questao_id IS NOT NULL;
-- descritiva: uma linha por questao
CREATE UNIQUE INDEX ux_resp_questao_texto
    ON avaliacao.resposta_questao (resposta_instrumento_id, sessao_questao_id) WHERE opcao_questao_id IS NULL;

-- ---------------------------------------------------------------------
-- 8. Encaminhamento (RF14): nenhum ciclo e consolidado sem registro
-- ---------------------------------------------------------------------
CREATE TABLE avaliacao.encaminhamento
(
    encaminhamento_id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    instrumento_avaliativo_id UUID        NOT NULL REFERENCES avaliacao.instrumento_avaliativo (instrumento_avaliativo_id),
    alvo_ref                  VARCHAR(60), -- ex.: docente/turma (RF-AD08)
    tipo                      VARCHAR(22) NOT NULL CHECK (tipo IN ('DECISAO', 'PLANO_ACAO', 'JUSTIFICATIVA_NAO_ACAO')),
    descricao                 TEXT        NOT NULL,
    responsavel_ref           VARCHAR(60) NOT NULL,
    prazo                     DATE,
    registrado_em             TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ---------------------------------------------------------------------
-- 9. Indices de apoio (FKs consultadas com frequencia)
-- ---------------------------------------------------------------------
CREATE INDEX ix_instrumento_sessao_instrumento
    ON avaliacao.instrumento_sessao (instrumento_avaliativo_id, instrumento_sessao_pai_id, ordem);
CREATE INDEX ix_sessao_questao_questao
    ON avaliacao.sessao_questao (questao_id);
CREATE INDEX ix_questao_conjunto
    ON avaliacao.questao (conjunto_opcao_id);
CREATE INDEX ix_resposta_instrumento_instrumento
    ON avaliacao.resposta_instrumento (instrumento_avaliativo_id, alvo_ref);
CREATE INDEX ix_resposta_questao_sessao_questao
    ON avaliacao.resposta_questao (sessao_questao_id);
CREATE INDEX ix_encaminhamento_instrumento
    ON avaliacao.encaminhamento (instrumento_avaliativo_id, alvo_ref);
