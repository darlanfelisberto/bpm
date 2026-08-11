import type { ErroValidacao, Instrumento, RespostaItem, RespostaStatus } from './types';

const BASE = '/api/avaliacao/instrumentos';

export class ErroApi extends Error {
  status: number;
  erros: string[];

  constructor(status: number, erros: string[]) {
    super(erros.join(' '));
    this.status = status;
    this.erros = erros;
  }
}

async function tratarErro(resp: Response): Promise<never> {
  let erros = [`Erro inesperado (HTTP ${resp.status}).`];
  try {
    const corpo = (await resp.json()) as ErroValidacao;
    if (corpo.erros?.length) {
      erros = corpo.erros;
    }
  } catch {
    // corpo sem JSON (ex.: 404 puro) - mantém a mensagem genérica
  }
  throw new ErroApi(resp.status, erros);
}

export async function buscarInstrumento(instrumentoId: string): Promise<Instrumento> {
  const resp = await fetch(`${BASE}/${instrumentoId}`);
  if (!resp.ok) {
    return tratarErro(resp);
  }
  return resp.json();
}

export async function buscarResposta(instrumentoId: string, token: string): Promise<RespostaStatus | null> {
  const resp = await fetch(`${BASE}/${instrumentoId}/respostas/${encodeURIComponent(token)}`);
  if (resp.status === 204) {
    return null;
  }
  if (!resp.ok) {
    return tratarErro(resp);
  }
  return resp.json();
}

export async function salvarResposta(
  instrumentoId: string,
  token: string,
  completo: boolean,
  respostas: RespostaItem[],
): Promise<RespostaStatus> {
  const resp = await fetch(`${BASE}/${instrumentoId}/respostas/${encodeURIComponent(token)}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ completo, respostas }),
  });
  if (!resp.ok) {
    return tratarErro(resp);
  }
  return resp.json();
}
