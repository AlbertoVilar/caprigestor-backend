package com.devmaster.goatfarm.farm.dao;

import com.devmaster.goatfarm.address.model.entity.Address;
import com.devmaster.goatfarm.address.model.repository.AddressRepository;
import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.converters.GoatFarmConverter;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.farm.model.repository.GoatFarmRepository;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.converter.GoatEntityConverter;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import com.devmaster.goatfarm.owner.model.entity.Owner;
import com.devmaster.goatfarm.owner.model.repository.OwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GoatFarmDAO {

    @Autowired
    private GoatFarmRepository goatFarmRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Transactional
    public GoatFarmResponseVO createGoatFarm(GoatFarmRequestVO requestVO) {
        if (requestVO == null) {
            throw new IllegalArgumentException("Os dados da fazenda para criação não podem ser nulos.");
        }

        if (goatFarmRepository.existsByName(requestVO.getName())) {
            throw new DuplicateEntityException("Já existe uma fazenda com o nome '" + requestVO.getName() + "'.");
        }

        if (requestVO.getTod() != null && goatFarmRepository.existsByTod(requestVO.getTod())) {
            throw new DuplicateEntityException("Já existe uma fazenda com o código '" + requestVO.getTod() + "'.");
        }

        Owner owner = ownerRepository.findById(requestVO.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Dono não encontrado com o ID: " + requestVO.getOwnerId()));
        Address address = addressRepository.findById(requestVO.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado com o ID: " + requestVO.getAddressId()));

        GoatFarm goatFarm = GoatFarmConverter.toEntity(requestVO, owner, address);

        try {
            goatFarm = goatFarmRepository.save(goatFarm);
            return GoatFarmConverter.toVO(goatFarm);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Ocorreu um erro ao salvar a fazenda: " + e.getMessage());
        }
    }

    @Transactional
    public GoatFarmResponseVO updateGoatFarm(Long id, GoatFarmRequestVO requestVO) {
        GoatFarm goatFarmToUpdate = goatFarmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fazenda com ID " + id + " não encontrada."));

        if (!goatFarmToUpdate.getName().equals(requestVO.getName()) &&
                goatFarmRepository.existsByName(requestVO.getName())) {
            throw new DuplicateEntityException("Já existe outra fazenda com o nome '" + requestVO.getName() + "'.");
        }

        if (requestVO.getTod() != null &&
                (goatFarmToUpdate.getTod() == null || !goatFarmToUpdate.getTod().equals(requestVO.getTod())) &&
                goatFarmRepository.existsByTod(requestVO.getTod())) {
            throw new DuplicateEntityException("Já existe outra fazenda com o código '" + requestVO.getTod() + "'.");
        }

        GoatFarmConverter.entityUpdate(goatFarmToUpdate, requestVO);
        try {
            GoatFarm updatedGoatFarm = goatFarmRepository.save(goatFarmToUpdate);
            return GoatFarmConverter.toVO(updatedGoatFarm);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Ocorreu um erro ao atualizar a fazenda com ID " + id + ": " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public GoatFarmFullResponseVO findGoatFarmById(Long id) {
        GoatFarm goatFarm = goatFarmRepository.findById(id) // usa EntityGraph agora
                .orElseThrow(() -> new ResourceNotFoundException("Fazenda com ID " + id + " não encontrada."));

        return GoatFarmConverter.toFullVO(goatFarm);
    }

    @Transactional(readOnly = true)
    public Page<GoatFarmFullResponseVO> searchGoatFarmByName(String name, Pageable pageable) {
        Page<GoatFarm> resultGoatFarms = goatFarmRepository.searchGoatFarmByName(name, pageable);
        return resultGoatFarms.map(GoatFarmConverter::toFullVO);
    }

    @Transactional(readOnly = true)
    public Page<GoatFarmFullResponseVO> findAllGoatFarm(Pageable pageable) {
        Page<GoatFarm> projections = goatFarmRepository.searchAllFullFarms(pageable);
        return projections.map(GoatFarmConverter::toFullVO);
    }


    @Transactional
    public String deleteGoatFarm(Long id) {
        if (!goatFarmRepository.existsById(id)) {
            throw new ResourceNotFoundException("Fazenda com ID " + id + " não encontrada.");
        }
        try {
            goatFarmRepository.deleteById(id);
            return "Fazenda com ID " + id + " foi deletada com sucesso.";
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Não é possível deletar a fazenda com ID " + id +
                    " porque ela possui relacionamentos com outras entidades.");
        }
    }

}
