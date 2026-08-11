import './style.css';
import { buscarInstrumento, buscarResposta, salvarResposta, ErroApi } from './api';
import { gerarTokenAnonimo, tokenAnonimoSalvo } from './token';
import type { Instrumento, Questao, RespostaItem, RespostaStatus } from './types';

const app = document.querySelector<HTMLDivElement>('#app')!;

function lerParametros(): { instrumentoId: string | null; ref: string | null } {
  const params = new URLSearchParams(window.location.search);
  return { instrumentoId: params.get('instrumentoId'), ref: params.get('ref') };
}

function atualizarUrlComRef(instrumentoId: string, ref: string): void {
  const url = new URL(window.location.href);
  url.searchParams.set('instrumentoId', instrumentoId);
  url.searchParams.set('ref', ref);
  window.history.replaceState({}, '', url);
}

function escapeHtml(valor: string): string {
  const div = document.createElement('div');
  div.textContent = valor;
  return div.innerHTML;
}

function renderCarregando(): void {
  app.innerHTML = `<p>Carregando...</p>`;
}

function renderErro(mensagem: string): void {
  app.innerHTML = `<div class="msg-error">${escapeHtml(mensagem)}</div>`;
}

async function iniciar(): Promise<void> {
  const { instrumentoId } = lerParametros();
  if (!instrumentoId) {
    renderErro('Link inválido: parâmetro instrumentoId ausente.');
    return;
  }

  renderCarregando();
  let instrumento: Instrumento;
  try {
    instrumento = await buscarInstrumento(instrumentoId);
  } catch (err) {
    renderErro(err instanceof ErroApi ? err.erros.join(' ') : 'Não foi possível carregar o instrumento.');
    return;
  }

  let { ref } = lerParametros();
  if (!ref && instrumento.anonimo) {
    ref = tokenAnonimoSalvo(instrumentoId);
    if (ref) {
      atualizarUrlComRef(instrumentoId, ref);
    }
  }

  if (!ref) {
    renderIdentificacao(instrumento, instrumentoId);
    return;
  }

  await renderFormulario(instrumento, instrumentoId, ref);
}

function renderIdentificacao(instrumento: Instrumento, instrumentoId: string): void {
  app.innerHTML = `
    <h1>${escapeHtml(instrumento.titulo)}</h1>
    ${instrumento.descricao ? `<p class="descricao">${escapeHtml(instrumento.descricao)}</p>` : ''}
    <div class="card" id="identificacao"></div>
  `;
  const container = document.querySelector<HTMLDivElement>('#identificacao')!;

  if (instrumento.anonimo) {
    container.innerHTML = `<button type="button" id="btn-iniciar">Iniciar avaliação anônima</button>`;
    container.querySelector('#btn-iniciar')!.addEventListener('click', () => {
      const token = gerarTokenAnonimo(instrumentoId);
      atualizarUrlComRef(instrumentoId, token);
      void renderFormulario(instrumento, instrumentoId, token);
    });
  } else {
    container.innerHTML = `
      <label for="ref">Identificação (e-mail ou matrícula)</label><br/>
      <input type="text" id="ref"/>
      <div style="margin-top: 0.75rem;">
        <button type="button" id="btn-continuar">Continuar</button>
      </div>
    `;
    container.querySelector('#btn-continuar')!.addEventListener('click', () => {
      const input = container.querySelector<HTMLInputElement>('#ref')!;
      const valor = input.value.trim();
      if (!valor) {
        input.focus();
        return;
      }
      atualizarUrlComRef(instrumentoId, valor);
      void renderFormulario(instrumento, instrumentoId, valor);
    });
  }
}

function renderQuestao(questao: Questao, respondida?: RespostaItem): string {
  const marcaObrigatoria = questao.obrigatoria ? ' <span class="obrigatoria">*</span>' : '';

  if (questao.tipo === 'TEXTO_LIVRE') {
    return `
      <div class="questao">
        <span class="enunciado">${escapeHtml(questao.enunciado)}${marcaObrigatoria}</span>
        <textarea name="questao-${questao.id}">${escapeHtml(respondida?.textoLivre ?? '')}</textarea>
      </div>
    `;
  }

  const opcoes = questao.opcoes
    .map(
      (opcao) => `
        <label>
          <input type="radio" name="questao-${questao.id}" value="${opcao.id}" ${
            respondida?.opcaoId === opcao.id ? 'checked' : ''
          }/>
          ${escapeHtml(opcao.texto)}
        </label>
      `,
    )
    .join('');

  return `
    <div class="questao">
      <span class="enunciado">${escapeHtml(questao.enunciado)}${marcaObrigatoria}</span>
      <div class="opcoes">${opcoes}</div>
    </div>
  `;
}

async function renderFormulario(instrumento: Instrumento, instrumentoId: string, token: string): Promise<void> {
  renderCarregando();

  let resposta: RespostaStatus | null;
  try {
    resposta = await buscarResposta(instrumentoId, token);
  } catch (err) {
    renderErro(err instanceof ErroApi ? err.erros.join(' ') : 'Não foi possível carregar sua resposta.');
    return;
  }

  const respostasPorQuestao = new Map<number, RespostaItem>();
  resposta?.respostas.forEach((item) => respostasPorQuestao.set(item.questaoId, item));

  const grupos = instrumento.grupos
    .map(
      (grupo) => `
        <h2>${escapeHtml(grupo.titulo)}</h2>
        ${grupo.descricao ? `<p class="descricao">${escapeHtml(grupo.descricao)}</p>` : ''}
        ${grupo.questoes.map((questao) => renderQuestao(questao, respostasPorQuestao.get(questao.id))).join('')}
      `,
    )
    .join('');

  app.innerHTML = `
    <h1>${escapeHtml(instrumento.titulo)}</h1>
    ${instrumento.descricao ? `<p class="descricao">${escapeHtml(instrumento.descricao)}</p>` : ''}
    ${
      resposta?.status === 'COMPLETO'
        ? `<div class="msg-info"><strong>Resposta já enviada.</strong> Enquanto o período de aplicação estiver aberto, você pode ajustar suas respostas e enviar novamente.</div>`
        : ''
    }
    <form class="card" id="form-resposta">
      ${grupos}
      <button type="button" class="secundario" id="btn-rascunho">Salvar rascunho</button>
      <button type="button" id="btn-enviar">Enviar</button>
      <span class="status" id="status"></span>
      <div id="mensagens"></div>
    </form>
  `;

  const form = document.querySelector<HTMLFormElement>('#form-resposta')!;
  const status = document.querySelector<HTMLSpanElement>('#status')!;
  const mensagens = document.querySelector<HTMLDivElement>('#mensagens')!;

  function coletarRespostas(): RespostaItem[] {
    return instrumento.grupos.flatMap((grupo) =>
      grupo.questoes.map((questao) => {
        if (questao.tipo === 'TEXTO_LIVRE') {
          const textarea = form.querySelector<HTMLTextAreaElement>(`[name="questao-${questao.id}"]`);
          return { questaoId: questao.id, opcaoId: null, textoLivre: textarea?.value.trim() || null };
        }
        const selecionado = form.querySelector<HTMLInputElement>(`[name="questao-${questao.id}"]:checked`);
        return {
          questaoId: questao.id,
          opcaoId: selecionado ? Number(selecionado.value) : null,
          textoLivre: null,
        };
      }),
    );
  }

  async function salvar(completo: boolean): Promise<void> {
    mensagens.innerHTML = '';
    status.textContent = completo ? 'Enviando...' : 'Salvando...';
    form.querySelectorAll('button').forEach((botao) => (botao.disabled = true));
    try {
      const resultado = await salvarResposta(instrumentoId, token, completo, coletarRespostas());
      status.textContent = completo ? 'Enviado.' : 'Rascunho salvo.';
      if (completo && resultado.status === 'COMPLETO') {
        await renderFormulario(instrumento, instrumentoId, token);
      }
    } catch (err) {
      status.textContent = '';
      const erros = err instanceof ErroApi ? err.erros : ['Não foi possível salvar. Tente novamente.'];
      mensagens.innerHTML = `<div class="msg-error"><ul>${erros
        .map((e) => `<li>${escapeHtml(e)}</li>`)
        .join('')}</ul></div>`;
    } finally {
      form.querySelectorAll('button').forEach((botao) => (botao.disabled = false));
    }
  }

  document.querySelector<HTMLButtonElement>('#btn-rascunho')!.addEventListener('click', () => {
    void salvar(false);
  });
  document.querySelector<HTMLButtonElement>('#btn-enviar')!.addEventListener('click', () => {
    void salvar(true);
  });
}

void iniciar();
