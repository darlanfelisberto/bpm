-- Núcleo genérico do módulo de Avaliação (schema isolado do BPM, mesmo banco).
CREATE SCHEMA IF NOT EXISTS avaliacao;

CREATE TABLE avaliacao.instrumento_avaliativo (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT,
    anonimo BOOLEAN NOT NULL DEFAULT FALSE,
    data_inicio TIMESTAMP NOT NULL,
    data_fim TIMESTAMP NOT NULL,
    publico_alvo_tipo VARCHAR(20) NOT NULL,
    publico_alvo_descricao VARCHAR(200),
    criado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE avaliacao.grupo_questao (
    id BIGSERIAL PRIMARY KEY,
    instrumento_id BIGINT NOT NULL REFERENCES avaliacao.instrumento_avaliativo(id) ON DELETE CASCADE,
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT,
    ordem INT NOT NULL DEFAULT 0
);

CREATE TABLE avaliacao.questao (
    id BIGSERIAL PRIMARY KEY,
    grupo_id BIGINT NOT NULL REFERENCES avaliacao.grupo_questao(id) ON DELETE CASCADE,
    enunciado TEXT NOT NULL,
    tipo VARCHAR(20) NOT NULL, -- ESCALA, MULTIPLA_ESCOLHA, TEXTO_LIVRE
    obrigatoria BOOLEAN NOT NULL DEFAULT TRUE,
    ordem INT NOT NULL DEFAULT 0
);

CREATE TABLE avaliacao.opcao_questao (
    id BIGSERIAL PRIMARY KEY,
    questao_id BIGINT NOT NULL REFERENCES avaliacao.questao(id) ON DELETE CASCADE,
    texto VARCHAR(200) NOT NULL,
    valor NUMERIC(10,2),
    ordem INT NOT NULL DEFAULT 0
);

CREATE TABLE avaliacao.resposta_instrumento (
    id BIGSERIAL PRIMARY KEY,
    instrumento_id BIGINT NOT NULL REFERENCES avaliacao.instrumento_avaliativo(id) ON DELETE CASCADE,
    respondente_ref VARCHAR(200), -- nulo quando o instrumento é anônimo
    status VARCHAR(20) NOT NULL DEFAULT 'PARCIAL', -- PARCIAL, COMPLETO
    iniciado_em TIMESTAMP NOT NULL DEFAULT now(),
    enviado_em TIMESTAMP
);

CREATE TABLE avaliacao.resposta_questao (
    id BIGSERIAL PRIMARY KEY,
    resposta_instrumento_id BIGINT NOT NULL REFERENCES avaliacao.resposta_instrumento(id) ON DELETE CASCADE,
    questao_id BIGINT NOT NULL REFERENCES avaliacao.questao(id) ON DELETE CASCADE,
    opcao_id BIGINT REFERENCES avaliacao.opcao_questao(id),
    texto_livre TEXT,
    UNIQUE (resposta_instrumento_id, questao_id)
);
