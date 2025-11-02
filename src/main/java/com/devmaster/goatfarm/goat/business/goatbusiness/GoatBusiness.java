package com.devmaster.goatfarm.goat.business.goatbusiness;

import com.devmaster.goatfarm.config.exceptions.DuplicateEntityException;
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
import org.springframework.transaction.annotation.Transactional;

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

    public GoatResponseVO createGoat(GoatRequestVO requestVO) {
        if (requestVO.getRegistrationNumber() != null &&
                goatRepository.existsById(requestVO.getRegistrationNumber())) {
            throw new DuplicateEntityException("Número de registro já existe.");
        }

        GoatFarm farm = findGoatFarmById(requestVO.getFarmId());
        Goat father = findOptionalGoat(requestVO.getFatherRegistrationNumber()).orElse(null);
        Goat mother = findOptionalGoat(requestVO.getMotherRegistrationNumber()).orElse(null);

        Goat goat = goatMapper.toEntity(requestVO);
        // Set usuário responsável pelo cadastro (usuario_id NOT NULL)
        Long reqUserId = requestVO.getUserId();
        if (reqUserId == null) {
            throw new com.devmaster.goatfarm.config.exceptions.custom.InvalidArgumentException("É obrigatório informar o usuário (userId) para cadastrar a cabra.");
        }
        User user = userRepository.findById(reqUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + reqUserId));
        goat.setUser(user);
        if (farm != null) goat.setFarm(farm);
        if (father != null) goat.setFather(father);
        if (mother != null) goat.setMother(mother);
        Goat savedGoat = goatRepository.save(goat);

        if (savedGoat.getRegistrationNumber() != null) {
            genealogyBusiness.createGenealogy(savedGoat.getRegistrationNumber());
        }

        return goatMapper.toResponseVO(savedGoat);
    }

    @Transactional
    public GoatResponseVO updateGoat(String registrationNumber, GoatRequestVO requestVO) {
        if (requestVO.getRegistrationNumber() != null && !registrationNumber.equalsIgnoreCase(requestVO.getRegistrationNumber())) {
            throw new DuplicateEntityException("Número de registro do path difere do body.");
        }

        GoatFarm farm = findGoatFarmById(requestVO.getFarmId());
        Goat father = findOptionalGoat(requestVO.getFatherRegistrationNumber()).orElse(null);
        Goat mother = findOptionalGoat(requestVO.getMotherRegistrationNumber()).orElse(null);

        return goatDAO.updateGoat(registrationNumber, requestVO, farm, father, mother);
    }

    @Transactional(readOnly = true)
    public GoatResponseVO findGoatByRegistrationNumber(String registrationNumber) {
        Goat goat = goatRepository.findByRegistrationNumber(registrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada."));
        return goatMapper.toResponseVO(goat);
    }

    @Transactional(readOnly = true)
    public Page<GoatResponseVO> searchGoatByNameAndFarmId(String goatName, Long farmId, Pageable pageable) {
        Page<Goat> goats = goatDAO.searchGoatByNameAndFarmId(goatName, farmId, pageable);
        return goats.map(goatMapper::toResponseVO);
    }

    @Transactional(readOnly = true)
    public Page<GoatResponseVO> findByFarmIdAndOptionalRegistrationNumber(Long farmId, String registrationNumber, Pageable pageable) {
        Page<Goat> goats = goatDAO.findByFarmIdAndOptionalRegistrationNumber(farmId, registrationNumber, pageable);
        return goats.map(goatMapper::toResponseVO);
    }

    // ============ Métodos adicionados para alinhar com GoatFacade ============

    public void deleteGoat(String registrationNumber) {
        goatDAO.deleteGoat(registrationNumber);
    }

    @Transactional(readOnly = true)
    public Page<GoatResponseVO> findAllGoats(Pageable pageable) {
        Page<Goat> goats = goatRepository.findAll(pageable);
        return goats.map(goatMapper::toResponseVO);
    }

    @Transactional(readOnly = true)
    public Page<GoatResponseVO> searchGoatByName(String name, Pageable pageable) {
        Page<Goat> goats = goatRepository.searchGoatByName(name, pageable);
        return goats.map(goatMapper::toResponseVO);
    }

    @Transactional(readOnly = true)
    public Page<GoatResponseVO> findGoatsByNameAndFarmId(Long farmId, String name, Pageable pageable) {
        Page<Goat> goats = goatRepository.findByNameAndFarmId(farmId, name, pageable);
        return goats.map(goatMapper::toResponseVO);
    }

    @Transactional(readOnly = true)
    public Page<GoatResponseVO> findGoatsByFarmIdAndRegistrationNumber(Long farmId, String registrationNumber, Pageable pageable) {
        Page<Goat> goats = goatDAO.findByFarmIdAndOptionalRegistrationNumber(farmId, registrationNumber, pageable);
        return goats.map(goatMapper::toResponseVO);
    }

    private GoatFarm findGoatFarmById(Long id) {
        return goatFarmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fazenda não encontrada."));
    }

    private Optional<Goat> findOptionalGoat(String registrationNumber) {
        if (registrationNumber == null) return Optional.empty();
        return goatRepository.findByRegistrationNumber(registrationNumber);
    }
}

