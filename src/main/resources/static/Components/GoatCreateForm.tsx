import React, { useState } from 'react';
import { createGoat, convertFormDataToBackend, formOptions } from '../goatConverter';
import { GoatRequestDTO, validateGoatData, createEmptyGoatRequest } from '../Models/goatRequestDTO';

interface GoatCreateFormProps {
  farmId: number;
  onSuccess?: (goat: any) => void;
  onCancel?: () => void;
}

const GoatCreateForm: React.FC<GoatCreateFormProps> = ({ farmId, onSuccess, onCancel }) => {
  const [formData, setFormData] = useState<GoatRequestDTO>(createEmptyGoatRequest(farmId));
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<string[]>([]);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    
    setFormData(prev => ({
      ...prev,
      [name]: name === 'weight' || name === 'height' || name === 'farmId' 
        ? parseFloat(value) || 0 
        : value
    }));
    
    // Limpar erros quando o usuário começar a digitar
    if (errors.length > 0) {
      setErrors([]);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Validar dados antes do envio
    const validationErrors = validateGoatData(formData);
    if (validationErrors.length > 0) {
      setErrors(validationErrors);
      return;
    }

    setLoading(true);
    setErrors([]);

    try {
      const token = localStorage.getItem('authToken');
      if (!token) {
        throw new Error('Token de autenticação não encontrado');
      }

      // IMPORTANTE: Converter dados do formulário para formato do backend
      // Isso garante que os enums sejam enviados com valores corretos
      const backendData = convertFormDataToBackend(formData);
      
      // IMPORTANTE: Fazer apenas POST - sem verificações GET prévias
      // Isso evita o erro 404 que estava ocorrendo
      const result = await createGoat(backendData, token);
      
      console.log('Animal cadastrado com sucesso:', result);
      
      if (onSuccess) {
        onSuccess(result);
      }
      
      // Resetar formulário após sucesso
      setFormData(createEmptyGoatRequest(farmId));
      
    } catch (error: any) {
      console.error('Erro ao cadastrar animal:', error);
      
      // Tratar diferentes tipos de erro
      if (error.message.includes('409')) {
        setErrors(['Número de registro já existe! Escolha outro número.']);
      } else if (error.message.includes('400')) {
        setErrors(['Dados inválidos. Verifique os campos obrigatórios.']);
      } else if (error.message.includes('401')) {
        setErrors(['Sessão expirada. Faça login novamente.']);
      } else if (error.message.includes('403')) {
        setErrors(['Você não tem permissão para cadastrar animais.']);
      } else {
        setErrors([`Erro ao cadastrar animal: ${error.message}`]);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="goat-create-form">
      <h2>Cadastrar Novo Animal</h2>
      
      {errors.length > 0 && (
        <div className="error-messages" style={{ color: 'red', marginBottom: '1rem' }}>
          <ul>
            {errors.map((error, index) => (
              <li key={index}>{error}</li>
            ))}
          </ul>
        </div>
      )}

      <form onSubmit={handleSubmit}>
        <div className="form-row">
          <div className="form-group">
            <label htmlFor="registrationNumber">Número de Registro *</label>
            <input
              type="text"
              id="registrationNumber"
              name="registrationNumber"
              value={formData.registrationNumber}
              onChange={handleInputChange}
              required
              placeholder="Ex: 1643225002"
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="name">Nome *</label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleInputChange}
              required
              placeholder="Nome do animal"
              disabled={loading}
            />
          </div>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="birthDate">Data de Nascimento *</label>
            <input
              type="date"
              id="birthDate"
              name="birthDate"
              value={formData.birthDate}
              onChange={handleInputChange}
              required
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="gender">Sexo *</label>
            <select
              id="gender"
              name="gender"
              value={formData.gender}
              onChange={handleInputChange}
              required
              disabled={loading}
            >
              <option value="">Selecione o sexo</option>
              {formOptions.gender.map(option => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="breed">Raça *</label>
            <select
              id="breed"
              name="breed"
              value={formData.breed}
              onChange={handleInputChange}
              required
              disabled={loading}
            >
              <option value="">Selecione a raça</option>
              {formOptions.breed.map(option => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="color">Cor *</label>
            <input
              type="text"
              id="color"
              name="color"
              value={formData.color}
              onChange={handleInputChange}
              required
              placeholder="Ex: Branca, Marrom"
              disabled={loading}
            />
          </div>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="weight">Peso (kg) *</label>
            <input
              type="number"
              id="weight"
              name="weight"
              value={formData.weight || ''}
              onChange={handleInputChange}
              required
              min="0"
              step="0.1"
              placeholder="Ex: 45.5"
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="height">Altura (cm) *</label>
            <input
              type="number"
              id="height"
              name="height"
              value={formData.height || ''}
              onChange={handleInputChange}
              required
              min="0"
              step="0.1"
              placeholder="Ex: 75.0"
              disabled={loading}
            />
          </div>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="fatherRegistrationNumber">Registro do Pai</label>
            <input
              type="text"
              id="fatherRegistrationNumber"
              name="fatherRegistrationNumber"
              value={formData.fatherRegistrationNumber || ''}
              onChange={handleInputChange}
              placeholder="Opcional"
              disabled={loading}
            />
          </div>

          <div className="form-group">
            <label htmlFor="motherRegistrationNumber">Registro da Mãe</label>
            <input
              type="text"
              id="motherRegistrationNumber"
              name="motherRegistrationNumber"
              value={formData.motherRegistrationNumber || ''}
              onChange={handleInputChange}
              placeholder="Opcional"
              disabled={loading}
            />
          </div>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="status">Status *</label>
            <select
              id="status"
              name="status"
              value={formData.status}
              onChange={handleInputChange}
              required
              disabled={loading}
            >
              <option value="">Selecione o status</option>
              {formOptions.status.map(option => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="category">Categoria *</label>
            <select
              id="category"
              name="category"
              value={formData.category || ''}
              onChange={handleInputChange}
              required
              disabled={loading}
            >
              <option value="">Selecione a categoria</option>
              {formOptions.category.map(option => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="form-group">
          <label htmlFor="observations">Observações</label>
          <textarea
            id="observations"
            name="observations"
            value={formData.observations || ''}
            onChange={handleInputChange}
            rows={3}
            placeholder="Observações adicionais (opcional)"
            disabled={loading}
          />
        </div>

        <div className="form-actions">
          <button 
            type="button" 
            onClick={onCancel}
            disabled={loading}
            className="btn-cancel"
          >
            Cancelar
          </button>
          <button 
            type="submit" 
            disabled={loading}
            className="btn-submit"
          >
            {loading ? 'Cadastrando...' : 'Cadastrar Animal'}
          </button>
        </div>
      </form>

      <style jsx>{`
        .goat-create-form {
          max-width: 800px;
          margin: 0 auto;
          padding: 2rem;
          background: white;
          border-radius: 8px;
          box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }

        .form-row {
          display: flex;
          gap: 1rem;
          margin-bottom: 1rem;
        }

        .form-group {
          flex: 1;
          display: flex;
          flex-direction: column;
        }

        label {
          margin-bottom: 0.5rem;
          font-weight: 500;
          color: #333;
        }

        input, select, textarea {
          padding: 0.75rem;
          border: 1px solid #ddd;
          border-radius: 4px;
          font-size: 1rem;
        }

        input:focus, select:focus, textarea:focus {
          outline: none;
          border-color: #007bff;
          box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
        }

        .form-actions {
          display: flex;
          gap: 1rem;
          justify-content: flex-end;
          margin-top: 2rem;
        }

        .btn-cancel, .btn-submit {
          padding: 0.75rem 1.5rem;
          border: none;
          border-radius: 4px;
          font-size: 1rem;
          cursor: pointer;
          transition: background-color 0.2s;
        }

        .btn-cancel {
          background-color: #6c757d;
          color: white;
        }

        .btn-cancel:hover:not(:disabled) {
          background-color: #5a6268;
        }

        .btn-submit {
          background-color: #007bff;
          color: white;
        }

        .btn-submit:hover:not(:disabled) {
          background-color: #0056b3;
        }

        .btn-cancel:disabled, .btn-submit:disabled {
          opacity: 0.6;
          cursor: not-allowed;
        }

        .error-messages {
          background-color: #f8d7da;
          border: 1px solid #f5c6cb;
          border-radius: 4px;
          padding: 1rem;
        }

        .error-messages ul {
          margin: 0;
          padding-left: 1.5rem;
        }

        @media (max-width: 768px) {
          .form-row {
            flex-direction: column;
          }
          
          .form-actions {
            flex-direction: column;
          }
        }
      `}</style>
    </div>
  );
};

export default GoatCreateForm;