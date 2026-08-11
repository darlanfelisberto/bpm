// O "token" identifica o preenchimento no navegador. Quando o instrumento é
// anônimo ele é um UUID sem nenhuma ligação com a pessoa (só serve para
// retomar um rascunho); quando não é anônimo, é o identificador que o
// respondente digitou (e-mail/matrícula).

function chaveLocalStorage(instrumentoId: string): string {
  return `avaliacao-token-${instrumentoId}`;
}

export function gerarTokenAnonimo(instrumentoId: string): string {
  const token = `anon-${crypto.randomUUID()}`;
  localStorage.setItem(chaveLocalStorage(instrumentoId), token);
  return token;
}

export function tokenAnonimoSalvo(instrumentoId: string): string | null {
  return localStorage.getItem(chaveLocalStorage(instrumentoId));
}
