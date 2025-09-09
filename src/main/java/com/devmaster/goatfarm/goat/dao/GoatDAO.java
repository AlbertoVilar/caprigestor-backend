package com.devmaster.goatfarm.goat.dao;

import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.farm.model.repository.GoatFarmRepository;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.converter.GoatEntityConverter;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GoatDAO {

    private final GoatRepository goatRepository;
    private final GoatFarmRepository goatFarmRepository;
    private final UserRepository userRepository;

    @Autowired
    public GoatDAO(GoatRepository goatRepository, GoatFarmRepository goatFarmRepository, UserRepository userRepository) {
        this.goatRepository = goatRepository;
        this.goatFarmRepository = goatFarmRepository;
        this.userRepository = userRepository;
    }

    /**
     * Cria uma nova cabra, associando-a ao proprietário e fazenda informados,
     * e vinculando-a a pai e mãe se existirem.
     * @param requestVO Dados da cabra a ser criada.
     * @param ownerId ID do proprietário.
     * @param farmId ID da fazenda.
     * @return GoatResponseVO da cabra criada.
     * @throws DuplicateEntityException se o número de registro já existe.
     * @throws ResourceNotFoundException se o proprietário ou a fazenda não forem encontrados.
     */
    @Transactional
    public GoatResponseVO createGoat(GoatRequestVO requestVO, Long farmId) {
        if (goatRepository.existsById(requestVO.getRegistrationNumber())) {
            throw new DuplicateEntityException("Este registro '" + requestVO.getRegistrationNumber() + "' já existe.");
        }

        Goat father = findOptionalGoat(requestVO.getFatherRegistrationNumber()).orElse(null);
        Goat mother = findOptionalGoat(requestVO.getMotherRegistrationNumber()).orElse(null);
        GoatFarm farm = findGoatFarmById(farmId);
        User user = userRepository.findById(requestVO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + requestVO.getUserId()));

        Goat goat = GoatEntityConverter.toEntity(requestVO, father, mother, user, farm);
        goat = goatRepository.save(goat);

        return GoatEntityConverter.toResponseVO(goat);
    }

    /**
     * Atualiza os dados de uma cabra existente.
     * @param registrationNumber Número de registro da cabra a ser atualizada.
     * @param requestVO Dados da atualização.
     * @return GoatResponseVO da cabra atualizada.
     * @throws ResourceNotFoundException se a cabra não for encontrada.
     */
    @Transactional
    public GoatResponseVO updateGoat(String registrationNumber, GoatRequestVO requestVO) {
        // Usa findById para obter a entidade ou Optional.empty(), permitindo um tratamento claro de "não encontrado".
        Goat goatToUpdate = goatRepository.findById(registrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Animal com registro '" + registrationNumber + "' não encontrado."));

        Goat father = findOptionalGoat(requestVO.getFatherRegistrationNumber()).orElse(null);
        Goat mother = findOptionalGoat(requestVO.getMotherRegistrationNumber()).orElse(null);
        GoatFarm farm = findOptionalGoatFarm(requestVO.getFarmId()).orElse(null); // Optional pois pode ser null no request

        GoatEntityConverter.updateGoatEntity(goatToUpdate, requestVO, father, mother, farm);
        goatRepository.save(goatToUpdate);

        return GoatEntityConverter.toResponseVO(goatToUpdate);
    }

    /**
     * Busca uma cabra pelo número de registro, carregando suas relações.
     * @param registrationNumber Número de registro da cabra.
     * @return GoatResponseVO da cabra encontrada.
     * @throws ResourceNotFoundException se o animal não for encontrado.
     */
    @Transactional(readOnly = true)
    public GoatResponseVO findGoatById(String registrationNumber) {
        Goat goat = goatRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Animal '" + registrationNumber + "' não encontrado."));
        return GoatEntityConverter.toResponseVO(goat);
    }

    /**
     * Busca paginada de cabras por nome, sem considerar a fazenda.
     * @param name Nome ou parte do nome da cabra.
     * @param pageable Objeto Pageable para paginação.
     * @return Página de GoatResponseVOs.
     */
    @Transactional(readOnly = true)
    public Page<GoatResponseVO> searchGoatByName(String name, Pageable pageable) {
        Page<Goat> goatResult = goatRepository.searchGoatByName(name, pageable);
        return goatResult.map(GoatEntityConverter::toResponseVO);
    }

    /**
     * Busca paginada de cabras por nome dentro de uma fazenda específica.
     * @param farmId ID da fazenda.
     * @param name Nome ou parte do nome da cabra.
     * @param pageable Objeto Pageable para paginação.
     * @return Página de GoatResponseVOs.
     */
    @Transactional(readOnly = true)
    public Page<GoatResponseVO> searchGoatByNameAndFarmId(Long farmId, String name, Pageable pageable) {
        Page<Goat> goatResult = goatRepository.findByNameAndFarmId(farmId, name, pageable);
        return goatResult.map(GoatEntityConverter::toResponseVO);
    }

    /**
     * Busca paginada de cabras por ID da fazenda e número de registro opcional.
     * @param farmId ID da fazenda.
     * @param registrationNumber Número de registro da cabra (opcional).
     * @param pageable Objeto Pageable para paginação.
     * @return Página de GoatResponseVOs.
     * @throws ResourceNotFoundException se nenhuma cabra for encontrada para os critérios.
     */
    @Transactional(readOnly = true)
    public Page<GoatResponseVO> findByFarmIdAndOptionalRegistrationNumber(
            Long farmId, String registrationNumber, Pageable pageable) {

        // Chama o método do repositório que agora usa a Native Query com CAST
        Page<Goat> goats = goatRepository.findByFarmIdAndOptionalRegistrationNumber(farmId, registrationNumber, pageable);

        if (goats.isEmpty()) {
            // Personalize a mensagem de erro conforme a necessidade, considerando que registrationNumber pode ser null
            String message = "Nenhuma cabra encontrada para a fazenda com ID " + farmId;
            if (registrationNumber != null && !registrationNumber.isBlank()) {
                message += " e registro '" + registrationNumber + "'.";
            } else {
                message += ".";
            }
            throw new ResourceNotFoundException(message);
        }

        return goats.map(GoatEntityConverter::toResponseVO);
    }

    /**
     * Lista todas as cabras cadastradas com paginação.
     * @param pageable Objeto Pageable para paginação.
     * @return Página de GoatResponseVOs.
     */
    @Transactional(readOnly = true)
    public Page<GoatResponseVO> findAllGoats(Pageable pageable) {
        Page<Goat> goatResult = goatRepository.findAll(pageable);
        return goatResult.map(GoatEntityConverter::toResponseVO);
    }

    /**
     * Remove uma cabra do sistema, desde que não esteja vinculada a outra cabra.
     * @param registrationNumber Número de registro da cabra a ser removida.
     * @throws ResourceNotFoundException se a cabra não for encontrada.
     * @throws DatabaseException se a cabra estiver referenciada por outro animal (violando integridade).
     */
    @Transactional(propagation = Propagation.SUPPORTS) // SUPPORTS permite que se junte a uma transação existente, ou rode sem uma
    public void deleteGoat(String registrationNumber) {
        if (!goatRepository.existsById(registrationNumber)) {
            throw new ResourceNotFoundException("Registro '" + registrationNumber + "' não encontrado.");
        }

        try {
            goatRepository.deleteById(registrationNumber);
        } catch (DataIntegrityViolationException e) {
            // Captura a exceção específica de violação de integridade de dados do Spring
            // e a encapsula em uma exceção de negócio mais amigável.
            throw new DatabaseException("Animal com registro '" + registrationNumber
                    + "' não pode ser deletado, pois está referenciado por outro animal.", e); // Inclui a causa original
        }
    }

    // --- Métodos Auxiliares Privados para Reuso ---

    /**
     * Busca um Goat opcionalmente. Usado para pai/mãe que podem ser nulos.
     */
    private Optional<Goat> findOptionalGoat(String registrationNumber) {
        if (registrationNumber == null || registrationNumber.isBlank()) {
            return Optional.empty();
        }
        return goatRepository.findById(registrationNumber);
    }



    /**
     * Busca um GoatFarm pelo ID ou lança ResourceNotFoundException.
     */
    private GoatFarm findGoatFarmById(Long farmId) {
        return goatFarmRepository.findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Capril com ID '" + farmId + "' não encontrado."));
    }

    /**
     * Busca um GoatFarm opcionalmente. Usado para o caso de update onde o farmId pode ser null.
     */
    private Optional<GoatFarm> findOptionalGoatFarm(Long farmId) {
        if (farmId == null) {
            return Optional.empty();
        }
        return goatFarmRepository.findById(farmId);
    }
}