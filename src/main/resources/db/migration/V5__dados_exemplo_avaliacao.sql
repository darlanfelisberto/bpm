-- Dados de exemplo para o núcleo de avaliação: instrumento de "Avaliação
-- Docente pelo Discente" (Módulo 1), com as duas etapas previstas em
-- RF-AD01 (autoavaliação do discente + avaliação do desempenho docente).

INSERT INTO avaliacao.instrumento_avaliativo
    (titulo, descricao, anonimo, data_inicio, data_fim, publico_alvo_tipo, publico_alvo_descricao)
VALUES (
    'Avaliação Docente pelo Discente - 2026/1',
    'Avaliação semestral aplicada por componente curricular cursado no período.',
    true,
    now(),
    now() + interval '30 days',
    'INDIVIDUO',
    'Discentes matriculados em componentes curriculares no semestre 2026/1'
);

INSERT INTO avaliacao.grupo_questao (instrumento_id, titulo, descricao, ordem)
SELECT id, 'Autoavaliação do discente', 'Reflexão sobre a própria participação no componente curricular.', 0
FROM avaliacao.instrumento_avaliativo WHERE titulo = 'Avaliação Docente pelo Discente - 2026/1'
UNION ALL
SELECT id, 'Avaliação do desempenho docente', 'Avaliação da atuação do docente no componente curricular.', 1
FROM avaliacao.instrumento_avaliativo WHERE titulo = 'Avaliação Docente pelo Discente - 2026/1';

-- Questões do grupo "Autoavaliação do discente"
INSERT INTO avaliacao.questao (grupo_id, enunciado, tipo, obrigatoria, ordem)
SELECT g.id, 'Como você avalia sua participação e frequência nas aulas?', 'ESCALA', true, 0
FROM avaliacao.grupo_questao g
JOIN avaliacao.instrumento_avaliativo i ON i.id = g.instrumento_id
WHERE i.titulo = 'Avaliação Docente pelo Discente - 2026/1' AND g.titulo = 'Autoavaliação do discente';

INSERT INTO avaliacao.questao (grupo_id, enunciado, tipo, obrigatoria, ordem)
SELECT g.id, 'Comentários sobre sua própria atuação no componente curricular', 'TEXTO_LIVRE', false, 1
FROM avaliacao.grupo_questao g
JOIN avaliacao.instrumento_avaliativo i ON i.id = g.instrumento_id
WHERE i.titulo = 'Avaliação Docente pelo Discente - 2026/1' AND g.titulo = 'Autoavaliação do discente';

-- Questões do grupo "Avaliação do desempenho docente"
INSERT INTO avaliacao.questao (grupo_id, enunciado, tipo, obrigatoria, ordem)
SELECT g.id, 'O docente demonstrou domínio do conteúdo ministrado?', 'ESCALA', true, 0
FROM avaliacao.grupo_questao g
JOIN avaliacao.instrumento_avaliativo i ON i.id = g.instrumento_id
WHERE i.titulo = 'Avaliação Docente pelo Discente - 2026/1' AND g.titulo = 'Avaliação do desempenho docente';

INSERT INTO avaliacao.questao (grupo_id, enunciado, tipo, obrigatoria, ordem)
SELECT g.id, 'O docente cumpriu o plano de ensino apresentado?', 'ESCALA', true, 1
FROM avaliacao.grupo_questao g
JOIN avaliacao.instrumento_avaliativo i ON i.id = g.instrumento_id
WHERE i.titulo = 'Avaliação Docente pelo Discente - 2026/1' AND g.titulo = 'Avaliação do desempenho docente';

INSERT INTO avaliacao.questao (grupo_id, enunciado, tipo, obrigatoria, ordem)
SELECT g.id, 'Comentários e sugestões para o docente', 'TEXTO_LIVRE', false, 2
FROM avaliacao.grupo_questao g
JOIN avaliacao.instrumento_avaliativo i ON i.id = g.instrumento_id
WHERE i.titulo = 'Avaliação Docente pelo Discente - 2026/1' AND g.titulo = 'Avaliação do desempenho docente';

-- Opções da escala (mesma escala 1-4 usada por todas as questões ESCALA)
INSERT INTO avaliacao.opcao_questao (questao_id, texto, valor, ordem)
SELECT q.id, opcoes.texto, opcoes.valor, opcoes.ordem
FROM avaliacao.questao q
JOIN avaliacao.grupo_questao g ON g.id = q.grupo_id
JOIN avaliacao.instrumento_avaliativo i ON i.id = g.instrumento_id
JOIN (VALUES
    ('Insuficiente', 1, 0),
    ('Regular', 2, 1),
    ('Boa', 3, 2),
    ('Excelente', 4, 3)
) AS opcoes(texto, valor, ordem) ON true
WHERE i.titulo = 'Avaliação Docente pelo Discente - 2026/1' AND q.tipo = 'ESCALA';
