package com.devmaster.goatfarm.goat.dao;

import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.converter.GoatEntityConverter;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository; // <<< MUDANÇA: Convenção para a camada DAO
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository // <<< MUDANÇA: Convenção para a camada DAO
public class GoatDAO {

    private final GoatRepository goatRepository;

    @Autowired
    public GoatDAO(GoatRepository goatRepository) {
        this.goatRepository = goatRepository;
    }

    /**
     * Salva uma entidade Goat no banco de dados.
     * @param goat Entidade Goat a ser salva.
     * @return A entidade Goat salva.
     */
    @Transactional
    public Goat saveGoat(Goat goat) {
        return goatRepository.save(goat);
    }

    /**
     * Atualiza os dados de uma cabra existente.
     * Este método agora recebe as entidades relacionadas (farm, father, mother) já buscadas
     * pela camada de negócio para garantir a correta associação.
     *
     * @param registrationNumber Número de registro da cabra a ser atualizada.
     * @param requestVO Dados da atualização.
     * @param farm A entidade GoatFarm a ser associada.
     * @param father A entidade Goat do pai a ser associada (pode ser null).
     * @param mother A entidade Goat da mãe a ser associada (pode ser null).
     * @return GoatResponseVO da cabra atualizada.
     * @throws ResourceNotFoundException se a cabra não for encontrada.
     */
    @Transactional
    // <<< MUDANÇA: A assinatura do método foi alterada para receber as entidades
    public GoatResponseVO updateGoat(String registrationNumber, GoatRequestVO requestVO, GoatFarm farm, Goat father, Goat mother) {
        Goat goatToUpdate = goatRepository.findById(registrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Animal com registro '" + registrationNumber + "' não encontrado."));

        // <<< MUDANÇA: As linhas que anulavam as associações foram removidas.
        // O método agora usa os objetos recebidos como parâmetro.
        GoatEntityConverter.updateGoatEntity(goatToUpdate, requestVO, father, mother, farm);

        goatRepository.save(goatToUpdate);

        return GoatEntityConverter.toResponseVO(goatToUpdate);
    }

    /**
     * Busca uma cabra pelo número de registro, carregando seus relacionamentos.
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
     * Busca paginada por cabras por nome, sem considerar a fazenda.
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
     * Busca paginada por cabras por nome dentro de uma fazenda específica.
     * @param farmId ID da fazenda.
     * @param name Nome ou parte do nome da cabra.
     * @param pageable Objeto Pageable para paginação.
     * @return Página de GoatResponseVOs.
     * @throws ResourceNotFoundException se nenhuma cabra for encontrada para os critérios.
     */
    @Transactional(readOnly = true)
    public Page<GoatResponseVO> searchGoatByNameAndFarmId(Long farmId, String name, Pageable pageable) {
        Page<Goat> goatResult = goatRepository.findByNameAndFarmId(farmId, name, pageable);
        
        if (goatResult.isEmpty()) {
            String message = "Nenhuma cabra encontrada para a fazenda com ID " + farmId;
            if (name != null && !name.isBlank()) {
                message += " com o nome '" + name + "'.";
            } else {
                message += ".";
            }
            throw new ResourceNotFoundException(message);
        }
        
        return goatResult.map(GoatEntityConverter::toResponseVO);
    }

    /**
     * Busca paginada por cabras por ID da fazenda e número de registro opcional.
     * @param farmId ID da fazenda.
     * @param registrationNumber Número de registro da cabra (opcional).
     * @param pageable Objeto Pageable para paginação.
     * @return Página de GoatResponseVOs.
     * @throws ResourceNotFoundException se nenhuma cabra for encontrada para os critérios.
     */
    @Transactional(readOnly = true)
    public Page<GoatResponseVO> findByFarmIdAndOptionalRegistrationNumber(
            Long farmId, String registrationNumber, Pageable pageable) {

        Page<Goat> goats = goatRepository.findByFarmIdAndOptionalRegistrationNumber(farmId, registrationNumber, pageable);

        if (goats.isEmpty()) {
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
     * Lista todas as cabras registradas com paginação.
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
     * @throws DatabaseException se a cabra for referenciada por outro animal (violando a integridade).
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public void deleteGoat(String registrationNumber) {
        if (!goatRepository.existsById(registrationNumber)) {
            throw new ResourceNotFoundException("Registro '" + registrationNumber + "' não encontrado.");
        }

        try {
            goatRepository.deleteById(registrationNumber);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Animal com registro '" + registrationNumber
                    + "' não pode ser deletado pois é referenciado por outro animal.", e);
        }
    }
}