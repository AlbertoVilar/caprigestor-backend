package com.devmaster.goatfarm.goat.business.goatbusiness;

import com.devmaster.goatfarm.config.exceptions.custom.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.farm.model.repository.GoatFarmRepository;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.converter.GoatEntityConverter;
import com.devmaster.goatfarm.goat.dao.GoatDAO;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.authority.model.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GoatBusiness {

    private final GoatDAO goatDAO;
    private final GoatRepository goatRepository;
    private final GoatFarmRepository goatFarmRepository;
    private final UserRepository userRepository;

    @Autowired
    public GoatBusiness(GoatDAO goatDAO, GoatRepository goatRepository, 
                       GoatFarmRepository goatFarmRepository, UserRepository userRepository) {
        this.goatDAO = goatDAO;
        this.goatRepository = goatRepository;
        this.goatFarmRepository = goatFarmRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new goat in the system.
     * Handles entity assembly and business logic validation.
     */
    public GoatResponseVO createGoat(GoatRequestVO requestVO, Long userId, Long farmId) {
        // Check if registration number already exists
        if (goatRepository.existsById(requestVO.getRegistrationNumber())) {
            throw new DuplicateEntityException("Este registro '" + requestVO.getRegistrationNumber() + "' já existe.");
        }

        // Find optional parent goats
        Goat father = findOptionalGoat(requestVO.getFatherRegistrationNumber()).orElse(null);
        Goat mother = findOptionalGoat(requestVO.getMotherRegistrationNumber()).orElse(null);
        
        // Find required entities
        GoatFarm farm = findGoatFarmById(farmId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + userId));

        // Assemble and save entity
        Goat goat = GoatEntityConverter.toEntity(requestVO, father, mother, user, farm);
        goat = goatDAO.saveGoat(goat);

        return GoatEntityConverter.toResponseVO(goat);
    }

    /**
     * Searches for a goat by registration number.
     * Delegates the operation to the DAO layer.
     */
    public GoatResponseVO findGoatByRegistrationNumber(String registrationNumber) {
        return goatDAO.findGoatById(registrationNumber);
    }

    /**
     * Lists all goats with pagination.
     * Delegates the operation to the DAO layer.
     */
    public Page<GoatResponseVO> findAllGoats(Pageable pageable) {
        return goatDAO.findAllGoats(pageable);
    }

    /**
     * Paginated search for goats by name (without considering farm).
     * Delegates the operation to the DAO layer.
     */
    public Page<GoatResponseVO> searchGoatByName(String name, Pageable pageable) {
        return goatDAO.searchGoatByName(name, pageable);
    }

    /**
     * Paginated search for goats by name within a specific farm.
     * Delegates the operation to the DAO layer.
     */
    public Page<GoatResponseVO> findGoatsByNameAndFarmId(Long farmId, String name, Pageable pageable) {
        return goatDAO.searchGoatByNameAndFarmId(farmId, name, pageable);
    }

    /**
     * Paginated search for goats by farm ID and registration number (optional).
     * Delegates the operation to the DAO layer.
     */
    public Page<GoatResponseVO> findGoatsByFarmIdAndRegistrationNumber(Long farmId,
                                                                       String registrationNumber,
                                                                       Pageable pageable) {
        // This method now calls the DAO, which in turn calls the Repository with the corrected Native Query.
        return goatDAO.findByFarmIdAndOptionalRegistrationNumber(farmId, registrationNumber, pageable);
    }

    /**
     * Updates data of an existing goat.
     * Delegates the operation to the DAO layer.
     */
    public GoatResponseVO updateGoat(String numRegistration, GoatRequestVO requestVO) {
        return goatDAO.updateGoat(numRegistration, requestVO);
    }

    /**
     * Removes a goat from the system.
     * Delegates the operation to the DAO layer.
     */
    public void deleteGoat(String registrationNumber) {
        goatDAO.deleteGoat(registrationNumber);
    }

    /**
     * Finds an optional goat by registration number.
     * @param registrationNumber Registration number to search for.
     * @return Optional containing the goat if found, empty otherwise.
     */
    private Optional<Goat> findOptionalGoat(String registrationNumber) {
        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            return Optional.empty();
        }
        return goatRepository.findById(registrationNumber);
    }

    /**
     * Finds a goat farm by ID.
     * @param farmId Farm ID to search for.
     * @return GoatFarm entity.
     * @throws ResourceNotFoundException if the farm is not found.
     */
    private GoatFarm findGoatFarmById(Long farmId) {
        return goatFarmRepository.findById(farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada com ID: " + farmId));
    }
}