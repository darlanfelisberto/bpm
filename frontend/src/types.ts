export type TipoQuestao = 'ESCALA' | 'MULTIPLA_ESCOLHA' | 'TEXTO_LIVRE';

export interface Opcao {
  id: number;
  texto: string;
}

export interface Questao {
  id: number;
  enunciado: string;
  tipo: TipoQuestao;
  obrigatoria: boolean;
  opcoes: Opcao[];
}

export interface Grupo {
  id: number;
  titulo: string;
  descricao: string | null;
  questoes: Questao[];
}

export interface Instrumento {
  id: number;
  titulo: string;
  descricao: string | null;
  anonimo: boolean;
  aberto: boolean;
  grupos: Grupo[];
}

export interface RespostaItem {
  questaoId: number;
  opcaoId: number | null;
  textoLivre: string | null;
}

export interface RespostaStatus {
  status: 'PARCIAL' | 'COMPLETO';
  respostas: RespostaItem[];
}

export interface ErroValidacao {
  erros: string[];
}
