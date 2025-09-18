import type { GoatRequestDTO } from "./Models/goatRequestDTO";

// ===== CONVERSORES DE ENUMS =====

/**
 * Converte valores do formulário (português) para valores do backend (enum)
 */
export const enumConverters = {
  // Categoria: Frontend → Backend
  categoryToBackend: (frontendValue: string): string => {
    const mapping: { [key: string]: string } = {
      'Puro de Origem': 'PO',
      'Puro por Avaliação': 'PA',
      'Puro por Cruza': 'PC'
    };
    return mapping[frontendValue] || frontendValue;
  },

  // Categoria: Backend → Frontend
  categoryToFrontend: (backendValue: string): string => {
    const mapping: { [key: string]: string } = {
      'PO': 'Puro de Origem',
      'PA': 'Puro por Avaliação',
      'PC': 'Puro por Cruza'
    };
    return mapping[backendValue] || backendValue;
  },

  // Sexo: Frontend → Backend
  genderToBackend: (frontendValue: string): string => {
    const mapping: { [key: string]: string } = {
      'Macho': 'MALE',
      'Fêmea': 'FEMALE',
      'M': 'MALE',
      'F': 'FEMALE'
    };
    return mapping[frontendValue] || frontendValue;
  },

  // Sexo: Backend → Frontend
  genderToFrontend: (backendValue: string): string => {
    const mapping: { [key: string]: string } = {
      'MALE': 'Macho',
      'FEMALE': 'Fêmea'
    };
    return mapping[backendValue] || backendValue;
  },

  // Status: Frontend → Backend
  statusToBackend: (frontendValue: string): string => {
    const mapping: { [key: string]: string } = {
      'Ativo': 'ATIVO',
      'Inativo': 'INACTIVE',
      'Falecido': 'DECEASED',
      'Vendido': 'SOLD',
      'ACTIVE': 'ATIVO' // Corrige valor incorreto comum
    };
    return mapping[frontendValue] || frontendValue;
  },

  // Status: Backend → Frontend
  statusToFrontend: (backendValue: string): string => {
    const mapping: { [key: string]: string } = {
      'ATIVO': 'Ativo',
      'INACTIVE': 'Inativo',
      'DECEASED': 'Falecido',
      'SOLD': 'Vendido'
    };
    return mapping[backendValue] || backendValue;
  },

  // Raça: Frontend → Backend (mantém valores do enum)
  breedToBackend: (frontendValue: string): string => {
    // As raças já devem vir com os valores corretos do enum
    // Mas podemos fazer algumas correções comuns
    const mapping: { [key: string]: string } = {
      'Alpina': 'ALPINA',
      'Alpine': 'ALPINE',
      'Anglo Nubiana': 'ANGLO_NUBIANA',
      'Murciana Granadina': 'MURCIANA_GRANADINA'
    };
    return mapping[frontendValue] || frontendValue;
  }
};

/**
 * Opções para selects do formulário
 */
export const formOptions = {
  category: [
    { value: 'PO', label: 'Puro de Origem' },
    { value: 'PA', label: 'Puro por Avaliação' },
    { value: 'PC', label: 'Puro por Cruza' }
  ],
  
  gender: [
    { value: 'MALE', label: 'Macho' },
    { value: 'FEMALE', label: 'Fêmea' }
  ],
  
  status: [
    { value: 'ATIVO', label: 'Ativo' },
    { value: 'INACTIVE', label: 'Inativo' },
    { value: 'DECEASED', label: 'Falecido' },
    { value: 'SOLD', label: 'Vendido' }
  ],
  
  breed: [
    { value: 'ALPINE', label: 'Alpina (Alpine)' },
    { value: 'ANGLO_NUBIANA', label: 'Anglo Nubiana' },
    { value: 'BOER', label: 'Boer' },
    { value: 'MESTIÇA', label: 'Mestiça' },
    { value: 'MURCIANA_GRANADINA', label: 'Murciana Granadina' },
    { value: 'ALPINA', label: 'Alpina' },
    { value: 'SAANEN', label: 'Saanen' },
    { value: 'TOGGENBURG', label: 'Toggenburg' }
  ]
};

/**
 * Converte dados do formulário para o formato correto do backend
 */
export const convertFormDataToBackend = (formData: any): GoatRequestDTO => {
  return {
    ...formData,
    gender: enumConverters.genderToBackend(formData.gender),
    status: enumConverters.statusToBackend(formData.status),
    category: enumConverters.categoryToBackend(formData.category),
    breed: enumConverters.breedToBackend(formData.breed)
  };
};

// ===== FUNÇÕES DE API =====

/**
 * Função para cadastrar uma nova cabra no sistema.
 * Remove verificações GET desnecessárias que causam erro 404.
 * @param goatData Dados da cabra para cadastro
 * @param token Token JWT para autenticação
 * @returns Promise com a resposta da API
 */
export const createGoat = async (goatData: GoatRequestDTO, token: string): Promise<any> => {
  try {
    // IMPORTANTE: Fazer apenas POST, sem verificações GET prévias
    const response = await fetch('http://localhost:8080/api/goatfarms/goats', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(goatData)
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Erro ${response.status}: ${errorText}`);
    }

    return await response.json();
  } catch (error) {
    console.error('Erro ao cadastrar animal:', error);
    throw error;
  }
};

/**
 * Função para buscar uma cabra por número de registro.
 * Use apenas quando necessário, não durante o cadastro.
 * @param registrationNumber Número de registro da cabra
 * @param token Token JWT para autenticação
 * @returns Promise com os dados da cabra
 */
export const getGoatByRegistration = async (registrationNumber: string, token: string): Promise<any> => {
  try {
    const response = await fetch(`http://localhost:8080/api/goatfarms/goats/registration/${registrationNumber}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    if (!response.ok) {
      if (response.status === 404) {
        return null; // Animal não encontrado
      }
      throw new Error(`Erro ${response.status}: ${response.statusText}`);
    }

    return await response.json();
  } catch (error) {
    console.error('Erro ao buscar animal:', error);
    throw error;
  }
};

/**
 * Função para listar cabras de uma fazenda.
 * @param farmId ID da fazenda
 * @param token Token JWT para autenticação
 * @param page Página (opcional, padrão 0)
 * @param size Tamanho da página (opcional, padrão 10)
 * @returns Promise com a lista paginada de cabras
 */
export const getGoatsByFarm = async (
  farmId: number, 
  token: string, 
  page: number = 0, 
  size: number = 10
): Promise<any> => {
  try {
    const response = await fetch(
      `http://localhost:8080/api/goatfarms/${farmId}/goats?page=${page}&size=${size}`, 
      {
        method: 'GET',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    );

    if (!response.ok) {
      throw new Error(`Erro ${response.status}: ${response.statusText}`);
    }

    return await response.json();
  } catch (error) {
    console.error('Erro ao listar animais:', error);
    throw error;
  }
};

/**
 * Função para atualizar uma cabra existente.
 * @param registrationNumber Número de registro da cabra
 * @param goatData Dados atualizados da cabra
 * @param token Token JWT para autenticação
 * @returns Promise com a resposta da API
 */
export const updateGoat = async (
  registrationNumber: string, 
  goatData: GoatRequestDTO, 
  token: string
): Promise<any> => {
  try {
    const response = await fetch(`http://localhost:8080/api/goatfarms/goats/${registrationNumber}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(goatData)
    });

    if (!response.ok) {
      const errorText = await response.text();
      throw new Error(`Erro ${response.status}: ${errorText}`);
    }

    return await response.json();
  } catch (error) {
    console.error('Erro ao atualizar animal:', error);
    throw error;
  }
};

/**
 * Função para deletar uma cabra.
 * @param registrationNumber Número de registro da cabra
 * @param token Token JWT para autenticação
 * @returns Promise void
 */
export const deleteGoat = async (registrationNumber: string, token: string): Promise<void> => {
  try {
    const response = await fetch(`http://localhost:8080/api/goatfarms/goats/${registrationNumber}`, {
      method: 'DELETE',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });

    if (!response.ok) {
      throw new Error(`Erro ${response.status}: ${response.statusText}`);
    }
  } catch (error) {
    console.error('Erro ao deletar animal:', error);
    throw error;
  }
};

// Exemplo de uso correto para cadastro:
/*
const handleCreateGoat = async (formData: GoatRequestDTO) => {
  try {
    const token = localStorage.getItem('authToken');
    if (!token) {
      throw new Error('Token de autenticação não encontrado');
    }

    // APENAS POST - sem verificações GET prévias
    const result = await createGoat(formData, token);
    console.log('Animal cadastrado com sucesso:', result);
    
    // Redirecionar ou mostrar mensagem de sucesso
  } catch (error) {
    if (error.message.includes('409')) {
      alert('Número de registro já existe!');
    } else if (error.message.includes('400')) {
      alert('Dados inválidos. Verifique os campos obrigatórios.');
    } else {
      alert('Erro ao cadastrar animal: ' + error.message);
    }
  }
};
*/