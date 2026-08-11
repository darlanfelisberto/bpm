-- ==========================================================
-- Metadados de alto nível
-- ==========================================================
CREATE TABLE macroprocesso (
    id              BIGSERIAL PRIMARY KEY,
    nome            VARCHAR(150) NOT NULL,
    objetivo        TEXT,
    criado_por      VARCHAR(100),   -- subject do token OIDC (Keycloak)
    criado_em       TIMESTAMP NOT NULL DEFAULT now(),
    atualizado_em   TIMESTAMP
);

CREATE TABLE processo (
    id                  BIGSERIAL PRIMARY KEY,
    macroprocesso_id    BIGINT NOT NULL REFERENCES macroprocesso(id) ON DELETE CASCADE,
    nome                VARCHAR(150) NOT NULL,
    descricao           TEXT,
    status              VARCHAR(20) NOT NULL DEFAULT 'RASCUNHO', -- RASCUNHO | PUBLICADO
    versao              INT NOT NULL DEFAULT 1,
    criado_por          VARCHAR(100),
    criado_em           TIMESTAMP NOT NULL DEFAULT now(),
    atualizado_em       TIMESTAMP,
    CONSTRAINT uq_processo_nome UNIQUE (macroprocesso_id, nome)
);

-- ==========================================================
-- Raias (pools/lanes) — representam o responsável dentro do processo
-- ==========================================================
CREATE TABLE raia (
    id              BIGSERIAL PRIMARY KEY,
    processo_id     BIGINT NOT NULL REFERENCES processo(id) ON DELETE CASCADE,
    nome            VARCHAR(120) NOT NULL,   -- ex: "Setor de RH", "Coordenador de Curso"
    ordem           INT NOT NULL DEFAULT 0,  -- posição vertical no diagrama
    CONSTRAINT uq_raia_processo UNIQUE (processo_id, nome)
);

-- ==========================================================
-- Tipos de elemento BPMN suportados (lookup)
-- ==========================================================
CREATE TABLE tipo_elemento (
    id              SERIAL PRIMARY KEY,
    codigo          VARCHAR(30) NOT NULL UNIQUE,  -- START_EVENT, END_EVENT, TASK, EXCLUSIVE_GATEWAY, PARALLEL_GATEWAY
    nome_exibicao   VARCHAR(60) NOT NULL,
    tag_bpmn        VARCHAR(60) NOT NULL          -- nome do elemento no XML: bpmn:task, bpmn:exclusiveGateway...
);

INSERT INTO tipo_elemento (codigo, nome_exibicao, tag_bpmn) VALUES
    ('START_EVENT',        'Início',              'bpmn:startEvent'),
    ('END_EVENT',          'Fim',                 'bpmn:endEvent'),
    ('TASK',               'Tarefa',              'bpmn:task'),
    ('EXCLUSIVE_GATEWAY',  'Decisão (Exclusivo)', 'bpmn:exclusiveGateway'),
    ('PARALLEL_GATEWAY',   'Paralelo',            'bpmn:parallelGateway');

-- ==========================================================
-- Atividades / elementos do processo
-- ==========================================================
CREATE TABLE atividade (
    id                  BIGSERIAL PRIMARY KEY,
    processo_id         BIGINT NOT NULL REFERENCES processo(id) ON DELETE CASCADE,
    raia_id             BIGINT REFERENCES raia(id) ON DELETE SET NULL,
    tipo_elemento_id    INT NOT NULL REFERENCES tipo_elemento(id),
    nome                VARCHAR(150) NOT NULL,   -- rótulo exibido no diagrama
    descricao           TEXT,
    ordem               INT NOT NULL DEFAULT 0,  -- ordenação na grid/lista
    criado_em           TIMESTAMP NOT NULL DEFAULT now(),
    atualizado_em       TIMESTAMP
);

CREATE INDEX ix_atividade_processo ON atividade(processo_id);

-- ==========================================================
-- Fluxos de sequência — arestas do grafo (origem -> destino)
-- Substitui uma coluna "proxima_atividade_id" para suportar
-- bifurcações em gateways.
-- ==========================================================
CREATE TABLE fluxo_sequencia (
    id                      BIGSERIAL PRIMARY KEY,
    processo_id             BIGINT NOT NULL REFERENCES processo(id) ON DELETE CASCADE,
    atividade_origem_id     BIGINT NOT NULL REFERENCES atividade(id) ON DELETE CASCADE,
    atividade_destino_id    BIGINT NOT NULL REFERENCES atividade(id) ON DELETE CASCADE,
    rotulo_condicao         VARCHAR(60),   -- ex: "Sim", "Não", "Aprovado" (usado em gateways)
    ordem                   INT NOT NULL DEFAULT 0,
    CONSTRAINT ck_fluxo_nao_reflexivo CHECK (atividade_origem_id <> atividade_destino_id),
    CONSTRAINT uq_fluxo UNIQUE (atividade_origem_id, atividade_destino_id)
);

CREATE INDEX ix_fluxo_origem  ON fluxo_sequencia(atividade_origem_id);
CREATE INDEX ix_fluxo_destino ON fluxo_sequencia(atividade_destino_id);

-- ==========================================================
-- Cache do XML BPMN gerado, para histórico/versionamento
-- ==========================================================
CREATE TABLE diagrama_gerado (
    id              BIGSERIAL PRIMARY KEY,
    processo_id     BIGINT NOT NULL REFERENCES processo(id) ON DELETE CASCADE,
    xml_bpmn        TEXT NOT NULL,
    gerado_em       TIMESTAMP NOT NULL DEFAULT now(),
    gerado_por      VARCHAR(100)
);

CREATE INDEX ix_diagrama_processo ON diagrama_gerado(processo_id);
