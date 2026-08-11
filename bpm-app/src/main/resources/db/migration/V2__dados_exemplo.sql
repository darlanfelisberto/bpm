-- Dados de exemplo para validar o CRUD e a geracao do diagrama BPMN.
-- Processo: "Solicitação de Férias", com uma bifurcação de gateway
-- (aprovado / indeferido) para exercitar o fluxo_sequencia com ramificação.

INSERT INTO macroprocesso (nome, objetivo) VALUES
    ('Gestão de Pessoas', 'Gerenciar processos relacionados a recursos humanos');

INSERT INTO processo (macroprocesso_id, nome, descricao, status)
SELECT id, 'Solicitação de Férias', 'Fluxo de solicitação e aprovação de férias do servidor', 'PUBLICADO'
FROM macroprocesso WHERE nome = 'Gestão de Pessoas';

-- Raias
INSERT INTO raia (processo_id, nome, ordem)
SELECT id, 'Servidor', 0 FROM processo WHERE nome = 'Solicitação de Férias'
UNION ALL
SELECT id, 'Chefia Imediata', 1 FROM processo WHERE nome = 'Solicitação de Férias'
UNION ALL
SELECT id, 'RH', 2 FROM processo WHERE nome = 'Solicitação de Férias';

-- Atividades
INSERT INTO atividade (processo_id, raia_id, tipo_elemento_id, nome, ordem)
SELECT p.id, r.id, t.id, 'Início', 1
FROM processo p
JOIN raia r ON r.processo_id = p.id AND r.nome = 'Servidor'
JOIN tipo_elemento t ON t.codigo = 'START_EVENT'
WHERE p.nome = 'Solicitação de Férias';

INSERT INTO atividade (processo_id, raia_id, tipo_elemento_id, nome, ordem)
SELECT p.id, r.id, t.id, 'Preencher formulário de férias', 2
FROM processo p
JOIN raia r ON r.processo_id = p.id AND r.nome = 'Servidor'
JOIN tipo_elemento t ON t.codigo = 'TASK'
WHERE p.nome = 'Solicitação de Férias';

INSERT INTO atividade (processo_id, raia_id, tipo_elemento_id, nome, ordem)
SELECT p.id, r.id, t.id, 'Aprovação da chefia?', 3
FROM processo p
JOIN raia r ON r.processo_id = p.id AND r.nome = 'Chefia Imediata'
JOIN tipo_elemento t ON t.codigo = 'EXCLUSIVE_GATEWAY'
WHERE p.nome = 'Solicitação de Férias';

INSERT INTO atividade (processo_id, raia_id, tipo_elemento_id, nome, ordem)
SELECT p.id, r.id, t.id, 'Registrar férias no sistema', 4
FROM processo p
JOIN raia r ON r.processo_id = p.id AND r.nome = 'RH'
JOIN tipo_elemento t ON t.codigo = 'TASK'
WHERE p.nome = 'Solicitação de Férias';

INSERT INTO atividade (processo_id, raia_id, tipo_elemento_id, nome, ordem)
SELECT p.id, r.id, t.id, 'Notificar indeferimento', 5
FROM processo p
JOIN raia r ON r.processo_id = p.id AND r.nome = 'Chefia Imediata'
JOIN tipo_elemento t ON t.codigo = 'TASK'
WHERE p.nome = 'Solicitação de Férias';

INSERT INTO atividade (processo_id, raia_id, tipo_elemento_id, nome, ordem)
SELECT p.id, r.id, t.id, 'Fim', 6
FROM processo p
JOIN raia r ON r.processo_id = p.id AND r.nome = 'RH'
JOIN tipo_elemento t ON t.codigo = 'END_EVENT'
WHERE p.nome = 'Solicitação de Férias';

-- Fluxos de sequência (arestas do grafo)
INSERT INTO fluxo_sequencia (processo_id, atividade_origem_id, atividade_destino_id, ordem)
SELECT p.id, o.id, d.id, 1
FROM processo p
JOIN atividade o ON o.processo_id = p.id AND o.nome = 'Início'
JOIN atividade d ON d.processo_id = p.id AND d.nome = 'Preencher formulário de férias'
WHERE p.nome = 'Solicitação de Férias';

INSERT INTO fluxo_sequencia (processo_id, atividade_origem_id, atividade_destino_id, ordem)
SELECT p.id, o.id, d.id, 2
FROM processo p
JOIN atividade o ON o.processo_id = p.id AND o.nome = 'Preencher formulário de férias'
JOIN atividade d ON d.processo_id = p.id AND d.nome = 'Aprovação da chefia?'
WHERE p.nome = 'Solicitação de Férias';

INSERT INTO fluxo_sequencia (processo_id, atividade_origem_id, atividade_destino_id, rotulo_condicao, ordem)
SELECT p.id, o.id, d.id, 'Aprovado', 3
FROM processo p
JOIN atividade o ON o.processo_id = p.id AND o.nome = 'Aprovação da chefia?'
JOIN atividade d ON d.processo_id = p.id AND d.nome = 'Registrar férias no sistema'
WHERE p.nome = 'Solicitação de Férias';

INSERT INTO fluxo_sequencia (processo_id, atividade_origem_id, atividade_destino_id, rotulo_condicao, ordem)
SELECT p.id, o.id, d.id, 'Indeferido', 4
FROM processo p
JOIN atividade o ON o.processo_id = p.id AND o.nome = 'Aprovação da chefia?'
JOIN atividade d ON d.processo_id = p.id AND d.nome = 'Notificar indeferimento'
WHERE p.nome = 'Solicitação de Férias';

INSERT INTO fluxo_sequencia (processo_id, atividade_origem_id, atividade_destino_id, ordem)
SELECT p.id, o.id, d.id, 5
FROM processo p
JOIN atividade o ON o.processo_id = p.id AND o.nome = 'Registrar férias no sistema'
JOIN atividade d ON d.processo_id = p.id AND d.nome = 'Fim'
WHERE p.nome = 'Solicitação de Férias';

INSERT INTO fluxo_sequencia (processo_id, atividade_origem_id, atividade_destino_id, ordem)
SELECT p.id, o.id, d.id, 6
FROM processo p
JOIN atividade o ON o.processo_id = p.id AND o.nome = 'Notificar indeferimento'
JOIN atividade d ON d.processo_id = p.id AND d.nome = 'Fim'
WHERE p.nome = 'Solicitação de Férias';
