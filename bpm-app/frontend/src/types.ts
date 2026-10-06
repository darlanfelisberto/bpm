export type TipoQuestao = 'ESCOLHA_UNICA' | 'MULTIPLA_ESCOLHA' | 'ESCALA' | 'DESCRITIVA';

export interface Opcao {
  id: string;
  texto: string;
}

export interface Questao {
  id: string;
  enunciado: string;
  tipo: TipoQuestao;
  obrigatoria: boolean;
  opcoes: Opcao[];
}

export interface Grupo {
  id: string;
  titulo: string;
  descricao: string | null;
  questoes: Questao[];
}

export interface Instrumento {
  id: string;
  titulo: string;
  descricao: string | null;
  anonimo: boolean;
  aberto: boolean;
  grupos: Grupo[];
}

export interface RespostaItem {
  questaoId: string;
  opcaoId: string | null;
  textoLivre: string | null;
}

export interface RespostaStatus {
  status: 'EM_ANDAMENTO' | 'ENVIADA';
  respostas: RespostaItem[];
}

export interface ErroValidacao {
  erros: string[];
}
