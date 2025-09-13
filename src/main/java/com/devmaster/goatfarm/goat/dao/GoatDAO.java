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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoatDAO {

    private final GoatRepository goatRepository;

    @Autowired
    public GoatDAO(GoatRepository goatRepository) {
        this.goatRepository = goatRepository;
    }

    /**
     * Saves a goat entity to the database.
     * @param goat Goat entity to be saved.
     * @return Saved goat entity.
     */
    @Transactional
    public Goat saveGoat(Goat goat) {
        return goatRepository.save(goat);
    }

    /**
     * Updates data of an existing goat.
     * @param registrationNumber Registration number of the goat to be updated.
     * @param requestVO Update data.
     * @return GoatResponseVO of the updated goat.
     * @throws ResourceNotFoundException if the goat is not found.
     */
    @Transactional
    public GoatResponseVO updateGoat(String registrationNumber, GoatRequestVO requestVO) {
        // Uses findById to get the entity or Optional.empty(), allowing clear handling of "not found".
        Goat goatToUpdate = goatRepository.findById(registrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Animal com registro '" + registrationNumber + "' não encontrado."));

        // Note: Parent relationships and farm assignment should be handled by the Business layer
        // This DAO method focuses only on updating the basic goat data
        Goat father = null; // Parent relationships handled by Business layer
        Goat mother = null; // Parent relationships handled by Business layer  
        GoatFarm farm = null; // Farm assignment handled by Business layer

        GoatEntityConverter.updateGoatEntity(goatToUpdate, requestVO, father, mother, farm);
        goatRepository.save(goatToUpdate);

        return GoatEntityConverter.toResponseVO(goatToUpdate);
    }

    /**
     * Searches for a goat by registration number, loading its relationships.
     * @param registrationNumber Goat registration number.
     * @return GoatResponseVO of the found goat.
     * @throws ResourceNotFoundException if the animal is not found.
     */
    @Transactional(readOnly = true)
    public GoatResponseVO findGoatById(String registrationNumber) {
        Goat goat = goatRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Animal '" + registrationNumber + "' não encontrado."));
        return GoatEntityConverter.toResponseVO(goat);
    }

    /**
     * Paginated search for goats by name, without considering the farm.
     * @param name Name or part of the goat's name.
     * @param pageable Pageable object for pagination.
     * @return Page of GoatResponseVOs.
     */
    @Transactional(readOnly = true)
    public Page<GoatResponseVO> searchGoatByName(String name, Pageable pageable) {
        Page<Goat> goatResult = goatRepository.searchGoatByName(name, pageable);
        return goatResult.map(GoatEntityConverter::toResponseVO);
    }

    /**
     * Paginated search for goats by name within a specific farm.
     * @param farmId Farm ID.
     * @param name Name or part of the goat's name.
     * @param pageable Pageable object for pagination.
     * @return Page of GoatResponseVOs.
     */
    @Transactional(readOnly = true)
    public Page<GoatResponseVO> searchGoatByNameAndFarmId(Long farmId, String name, Pageable pageable) {
        Page<Goat> goatResult = goatRepository.findByNameAndFarmId(farmId, name, pageable);
        return goatResult.map(GoatEntityConverter::toResponseVO);
    }

    /**
     * Paginated search for goats by farm ID and optional registration number.
     * @param farmId Farm ID.
     * @param registrationNumber Goat registration number (optional).
     * @param pageable Pageable object for pagination.
     * @return Page of GoatResponseVOs.
     * @throws ResourceNotFoundException if no goat is found for the criteria.
     */
    @Transactional(readOnly = true)
    public Page<GoatResponseVO> findByFarmIdAndOptionalRegistrationNumber(
            Long farmId, String registrationNumber, Pageable pageable) {

        // Calls the repository method that now uses Native Query with CAST
        Page<Goat> goats = goatRepository.findByFarmIdAndOptionalRegistrationNumber(farmId, registrationNumber, pageable);

        if (goats.isEmpty()) {
            // Customize the error message as needed, considering that registrationNumber can be null
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
     * Lists all registered goats with pagination.
     * @param pageable Pageable object for pagination.
     * @return Page of GoatResponseVOs.
     */
    @Transactional(readOnly = true)
    public Page<GoatResponseVO> findAllGoats(Pageable pageable) {
        Page<Goat> goatResult = goatRepository.findAll(pageable);
        return goatResult.map(GoatEntityConverter::toResponseVO);
    }

    /**
     * Removes a goat from the system, as long as it is not linked to another goat.
     * @param registrationNumber Registration number of the goat to be removed.
     * @throws ResourceNotFoundException if the goat is not found.
     * @throws DatabaseException if the goat is referenced by another animal (violating integrity).
     */
    @Transactional(propagation = Propagation.SUPPORTS) // SUPPORTS allows joining an existing transaction, or running without one
    public void deleteGoat(String registrationNumber) {
        if (!goatRepository.existsById(registrationNumber)) {
            throw new ResourceNotFoundException("Registro '" + registrationNumber + "' não encontrado.");
        }

        try {
            goatRepository.deleteById(registrationNumber);
        } catch (DataIntegrityViolationException e) {
            // Captures Spring's specific data integrity violation exception
            // and wraps it in a more user-friendly business exception.
            throw new DatabaseException("Animal com registro '" + registrationNumber
                    + "' cannot be deleted because it is referenced by another animal.", e); // Includes the original cause
        }
    }

    // --- Private Helper Methods for Reuse ---

}