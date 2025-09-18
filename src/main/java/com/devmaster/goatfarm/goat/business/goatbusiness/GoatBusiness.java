package com.devmaster.goatfarm.goat.business.goatbusiness;

import com.devmaster.goatfarm.config.exceptions.custom.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.farm.model.repository.GoatFarmRepository;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.mapper.GoatMapper;
import com.devmaster.goatfarm.goat.dao.GoatDAO;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
import com.devmaster.goatfarm.genealogy.business.genealogyservice.GenealogyBusiness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <<< MUDANÇA: Import necessário

import java.util.Optional;

@Service
public class GoatBusiness {

    private final GoatDAO goatDAO;
    private final GoatRepository goatRepository;
    private final GoatFarmRepository goatFarmRepository;
    private final UserRepository userRepository;
    private final GenealogyBusiness genealogyBusiness;
    private final GoatMapper goatMapper;

    @Autowired
    public GoatBusiness(GoatDAO goatDAO, GoatRepository goatRepository,
                        GoatFarmRepository goatFarmRepository, UserRepository userRepository,
                        GenealogyBusiness genealogyBusiness, GoatMapper goatMapper) {
        this.goatDAO = goatDAO;
        this.goatRepository = goatRepository;
        this.goatFarmRepository = goatFarmRepository;
        this.userRepository = userRepository;
        this.genealogyBusiness = genealogyBusiness;
        this.goatMapper = goatMapper;
    }

    /**
     * Cria uma nova cabra no sistema.
     * Monta a entidade e valida as regras de negócio.
     * Cria automaticamente a genealogia para a nova cabra.
     */
    @Transactional
    public GoatResponseVO createGoat(GoatRequestVO requestVO, Long userId, Long farmId) {
        // Verifica se o número de registro já existe
        if (goatRepository.existsById(requestVO.getRegistrationNumber())) {
            throw new DuplicateEntityException("Este registro '" + requestVO.getRegistrationNumber() + "' já existe.");
        }

        // Busca as cabras pai e mãe (opcionais)
        Goat father = findOptionalGoat(requestVO.getFatherRegistrationNumber()).orElse(null);
        Goat mother = findOptionalGoat(requestVO.getMotherRegistrationNumber()).orElse(null);

        // Busca as entidades obrigatórias
        GoatFarm farm = findGoatFarmById(farmId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + userId));

        // Monta e salva a entidade
        Goat goat = goatMapper.toEntity(requestVO);
        goat.setFather(father);
        goat.setMother(mother);
        goat.setFarm(farm);
        goat.setUser(user);
        goat = goatDAO.saveGoat(goat);

        // Cria automaticamente a genealogia para a nova cabra
        try {
            genealogyBusiness.createGenealogy(goat.getRegistrationNumber());
        } catch (Exception e) {
            // Loga o erro mas não falha a criação da cabra
            System.err.println("Aviso: Não foi possível criar a genealogia automaticamente para a cabra "
                    + goat.getRegistrationNumber() + ": " + e.getMessage());
        }

        return goatMapper.toResponseVO(goat);
    }

    /**
     * Atualiza os dados de uma cabra existente.
     * Esta camada agora é responsável por buscar as entidades relacionadas (Fazenda, Pai, Mãe)
     * e passá-las para a camada DAO para garantir que as associações sejam mantidas.
     */
    @Transactional // <<< MUDANÇA: Adicionada anotação de transação
    public GoatResponseVO updateGoat(String registrationNumber, GoatRequestVO requestVO) {
        // <<< MUDANÇA: Lógica de atualização foi adicionada aqui

        // Validação de negócio: registrationNumber do path deve ser igual ao do body (se informado)
        if (requestVO.getRegistrationNumber() != null && !registrationNumber.equalsIgnoreCase(requestVO.getRegistrationNumber())) {
            throw new DuplicateEntityException("Número de registro do path difere do body.");
        }

        // 1. Busca a fazenda obrigatória
        GoatFarm farm = findGoatFarmById(requestVO.getFarmId());

        // 2. Busca os pais (opcionais)
        Goat father = findOptionalGoat(requestVO.getFatherRegistrationNumber()).orElse(null);
        Goat mother = findOptionalGoat(requestVO.getMotherRegistrationNumber()).orElse(null);

        // 3. CORREÇÃO: Removida criação desnecessária de entidade Goat
        // O DAO é responsável por buscar e atualizar a entidade existente
        return goatDAO.updateGoat(registrationNumber, requestVO, farm, father, mother);
    }


    // =================================================================
    // MÉTODOS DE BUSCA E DELEÇÃO (sem alterações)
    // =================================================================

    /**
     * Busca uma cabra pelo número de registro.
     */
    public GoatResponseVO findGoatByRegistrationNumber(String registrationNumber) {
        return goatDAO.findGoatById(registrationNumber);
    }

    /**
     * Lista todas as cabras com paginação.
     */
    public Page<GoatResponseVO> findAllGoats(Pageable pageable) {
        return goatDAO.findAllGoats(pageable);
    }

    /**
     * Busca paginada de cabras por nome (sem considerar a fazenda).
     */
    public Page<GoatResponseVO> searchGoatByName(String name, Pageable pageable) {
        return goatDAO.searchGoatByName(name, pageable);
    }

    /**
     * Busca paginada de cabras por nome dentro de uma fazenda específica.
     */
    public Page<GoatResponseVO> findGoatsByNameAndFarmId(Long farmId, String name, Pageable pageable) {
        return goatDAO.searchGoatByNameAndFarmId(farmId, name, pageable);
    }

    /**
     * Busca paginada de cabras por ID da fazenda e número de registro (opcional).
     */
    public Page<GoatResponseVO> findGoatsByFarmIdAndRegistrationNumber(Long farmId,
                                                                       String registrationNumber,
                                                                       Pageable pageable) {
        return goatDAO.findByFarmIdAndOptionalRegistrationNumber(farmId, registrationNumber, pageable);
    }

    /**
     * Remove uma cabra do sistema.
     */
    public void deleteGoat(String registrationNumber) {
        goatDAO.deleteGoat(registrationNumber);
    }

    // =================================================================
    // MÉTODOS AUXILIARES (sem alterações)
    // =================================================================

    private Optional<Goat> findOptionalGoat(String registrationNumber) {
        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            return Optional.empty();
        }
        return goatRepository.findById(registrationNumber);
    }

    private GoatFarm findGoatFarmById(Long farmId) {
        if (farmId == null) {
            throw new ResourceNotFoundException("O ID da fazenda não pode ser nulo.");
        }
        return goatFarmRepository.findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada com ID: " + farmId));
    }
}

