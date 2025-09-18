/**
 * Interface para dados de requisição de cadastro/atualização de cabra.
 * Baseado no GoatRequestDTO do backend Java.
 */
export interface GoatRequestDTO {
  /** Número de registro único da cabra */
  registrationNumber: string;
  
  /** Nome da cabra */
  name: string;
  
  /** Data de nascimento no formato YYYY-MM-DD */
  birthDate: string;
  
  /** Sexo da cabra: 'MALE' para macho, 'FEMALE' para fêmea */
  gender: 'MALE' | 'FEMALE';
  
  /** Raça da cabra */
  breed: string;
  
  /** Cor da cabra */
  color: string;
  
  /** Peso da cabra em kg (opcional) */
  weight?: number;
  
  /** Altura da cabra em cm (opcional) */
  height?: number;
  
  /** Número de registro do pai (opcional) */
  fatherRegistrationNumber?: string;
  
  /** Número de registro da mãe (opcional) */
  motherRegistrationNumber?: string;
  
  /** Observações adicionais (opcional) */
  observations?: string;
  
  /** Status da cabra: 'ATIVO', 'INACTIVE', 'SOLD', 'DECEASED' */
  status: 'ATIVO' | 'INACTIVE' | 'SOLD' | 'DECEASED';
  
  /** Categoria da cabra: 'PO', 'PA', 'PC' */
  category: 'PO' | 'PA' | 'PC';
  
  /** TOD (Tatuagem Orelha Direita) - opcional */
  tod?: string;
  
  /** TOE (Tatuagem Orelha Esquerda) - opcional */
  toe?: string;
  
  /** ID do usuário responsável */
  userId?: number;
  
  /** ID da fazenda onde a cabra está registrada */
  farmId: number;
}

/**
 * Interface para resposta da API após cadastro/atualização de cabra.
 */
export interface GoatResponseDTO {
  /** ID único da cabra no sistema */
  id: number;
  
  /** Número de registro único da cabra */
  registrationNumber: string;
  
  /** Nome da cabra */
  name: string;
  
  /** Data de nascimento */
  birthDate: string;
  
  /** Sexo da cabra */
  gender: string;
  
  /** Raça da cabra */
  breed: string;
  
  /** Cor da cabra */
  color: string;
  
  /** Peso da cabra em kg */
  weight: number;
  
  /** Altura da cabra em cm */
  height: number;
  
  /** Dados do pai (se existir) */
  father?: {
    id: number;
    registrationNumber: string;
    name: string;
  };
  
  /** Dados da mãe (se existir) */
  mother?: {
    id: number;
    registrationNumber: string;
    name: string;
  };
  
  /** Observações adicionais */
  observations?: string;
  
  /** Status da cabra */
  status: string;
  
  /** Data de criação do registro */
  createdAt: string;
  
  /** Data da última atualização */
  updatedAt: string;
  
  /** Dados da fazenda */
  farm: {
    id: number;
    name: string;
  };
}

/**
 * Função utilitária para validar dados de GoatRequestDTO antes do envio.
 * @param goatData Dados da cabra para validação
 * @returns Array de erros encontrados (vazio se válido)
 */
export const validateGoatData = (goatData: GoatRequestDTO): string[] => {
  const errors: string[] = [];
  
  // Validações obrigatórias
  if (!goatData.registrationNumber?.trim()) {
    errors.push('Número de registro é obrigatório');
  }
  
  if (!goatData.name?.trim()) {
    errors.push('Nome é obrigatório');
  }
  
  if (!goatData.birthDate) {
    errors.push('Data de nascimento é obrigatória');
  }
  
  if (!goatData.gender || !['M', 'F'].includes(goatData.gender)) {
    errors.push('Sexo deve ser M (macho) ou F (fêmea)');
  }
  
  if (!goatData.breed?.trim()) {
    errors.push('Raça é obrigatória');
  }
  
  if (!goatData.color?.trim()) {
    errors.push('Cor é obrigatória');
  }
  
  if (!goatData.weight || goatData.weight <= 0) {
    errors.push('Peso deve ser maior que zero');
  }
  
  if (!goatData.height || goatData.height <= 0) {
    errors.push('Altura deve ser maior que zero');
  }
  
  if (!goatData.status || !['ACTIVE', 'INACTIVE', 'SOLD', 'DECEASED'].includes(goatData.status)) {
    errors.push('Status deve ser ACTIVE, INACTIVE, SOLD ou DECEASED');
  }
  
  if (!goatData.farmId || goatData.farmId <= 0) {
    errors.push('ID da fazenda é obrigatório e deve ser maior que zero');
  }
  
  // Validação de formato de data
  if (goatData.birthDate && !/^\d{4}-\d{2}-\d{2}$/.test(goatData.birthDate)) {
    errors.push('Data de nascimento deve estar no formato YYYY-MM-DD');
  }
  
  // Validação de número de registro (apenas números e letras)
  if (goatData.registrationNumber && !/^[A-Za-z0-9]+$/.test(goatData.registrationNumber)) {
    errors.push('Número de registro deve conter apenas letras e números');
  }
  
  return errors;
};

/**
 * Função utilitária para criar um objeto GoatRequestDTO com valores padrão.
 * @param farmId ID da fazenda
 * @returns Objeto GoatRequestDTO com valores padrão
 */
export const createEmptyGoatRequest = (farmId: number): GoatRequestDTO => {
  return {
    registrationNumber: '',
    name: '',
    birthDate: '',
    gender: 'FEMALE',
    breed: '',
    color: '',
    status: 'ATIVO',
    category: 'PO',
    farmId: farmId
  };
};