package com.devmaster.goatfarm.farm.dao;

import com.devmaster.goatfarm.address.model.entity.Address;
import com.devmaster.goatfarm.address.model.repository.AddressRepository;
import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.converter.GoatFarmConverter;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.farm.model.repository.GoatFarmRepository;
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
    private OwnerRepository ownerRepository; // Injeta OwnerDAO


    @Transactional
    public GoatFarmResponseVO createGoatFarm(GoatFarmRequestVO requestVO) {
        // Verifica se os dados da requisição são válidos
        if (requestVO == null) {
            throw new IllegalArgumentException("Os dados da fazenda para criação não podem ser nulos.");
        }

        // Verifica se já existe uma fazenda com o mesmo nome
        if (goatFarmRepository.existsByName(requestVO.getName())) {
            throw new DuplicateEntityException("Já existe uma fazenda com o nome '" + requestVO.getName() + "'.");
        }

        // Verifica se já existe uma fazenda com o mesmo código (TOD)
        if (requestVO.getTod() != null && goatFarmRepository.existsByTod(requestVO.getTod())) {
            throw new DuplicateEntityException("Já existe uma fazenda com o código '" + requestVO.getTod() + "'.");
        }

        // Busca Owner e Address por ID
        Owner owner = ownerRepository.findById(requestVO.getOwnerId()) // Usa getOwnerId()
                .orElseThrow(() -> new ResourceNotFoundException("Dono não encontrado com o ID: " + requestVO.getOwnerId()));
        Address address = addressRepository.findById(requestVO.getAddressId()) // Usa getAddressId()
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado com o ID: " + requestVO.getAddressId()));

        // Converte o VO da requisição para a entidade, passando Owner e Address
        GoatFarm goatFarm = GoatFarmConverter.toEntity(requestVO, owner, address);

        try {
            // Salva a nova fazenda
            goatFarm = goatFarmRepository.save(goatFarm);
            // Converte a entidade salva de volta para o VO de resposta
            return GoatFarmConverter.toVO(goatFarm);
        } catch (DataIntegrityViolationException e) {
            // Trata possíveis violações de integridade do banco de dados durante a criação
            throw new DatabaseException("Ocorreu um erro ao salvar a fazenda: " + e.getMessage());
        }
    }


    @Transactional
    public GoatFarmResponseVO updateGoatFarm(Long id, GoatFarmRequestVO requestVO) {
        // Busca a fazenda a ser atualizada pelo seu ID
        GoatFarm goatFarmToUpdate = goatFarmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fazenda com ID " + id + " não encontrada."));

        // Verifica se o novo nome já existe para outra fazenda
        if (!goatFarmToUpdate.getName().equals(requestVO.getName()) && goatFarmRepository.existsByName(requestVO.getName())) {
            throw new DuplicateEntityException("Já existe outra fazenda com o nome '" + requestVO.getName() + "'.");
        }

        // Verifica se o novo código (TOD) já existe para outra fazenda (se fornecido e alterado)
        if (requestVO.getTod() != null && (goatFarmToUpdate.getTod() == null || !goatFarmToUpdate.getTod().equals(requestVO.getTod())) && goatFarmRepository.existsByTod(requestVO.getTod())) {
            throw new DuplicateEntityException("Já existe outra fazenda com o código '" + requestVO.getTod() + "'.");
        }

        // Atualiza a entidade com os valores do VO da requisição
        GoatFarmConverter.entityUpdate(goatFarmToUpdate, requestVO);
        try {
            // Salva a fazenda atualizada
            GoatFarm updatedGoatFarm = goatFarmRepository.save(goatFarmToUpdate);
            // Converte a entidade atualizada de volta para o VO de resposta
            return GoatFarmConverter.toVO(updatedGoatFarm);
        } catch (DataIntegrityViolationException e) {
            // Trata possíveis violações de integridade do banco de dados durante a atualização
            throw new DatabaseException("Ocorreu um erro ao atualizar a fazenda com ID " + id + ": " + e.getMessage());
        }
    }

    @Transactional
    public GoatFarmResponseVO findGoatFarmById(Long id) {
        // Busca a fazenda pelo seu ID
        GoatFarm goatFarm = goatFarmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fazenda com ID " + id + " não encontrada."));

        return GoatFarmConverter.toVO(goatFarm);
    }

    @Transactional
    public Page<GoatFarmResponseVO> searchGoatFarmByName(String name, Pageable pageable) {

        Page<GoatFarm> resultGoatFarms = goatFarmRepository.searchGoatFarmByName(name, pageable);

        return resultGoatFarms.map(GoatFarmConverter::toVO);

    }

    @Transactional
    public Page<GoatFarmResponseVO> findAllGoatFarm(Pageable pageable) {

        Page<GoatFarm> resultGoatFarms = goatFarmRepository.findAll(pageable);
        // Converte a lista de entidades para uma lista de VOs de resposta
        return resultGoatFarms.map(GoatFarmConverter::toVO);

    }

    @Transactional
    public String deleteGoatFarm(Long id) {
        // Verifica se a fazenda existe antes de tentar deletar
        if (!goatFarmRepository.existsById(id)) {
            throw new ResourceNotFoundException("Fazenda com ID " + id + " não encontrada.");
        }
        try {
            // Deleta a fazenda pelo seu ID
            goatFarmRepository.deleteById(id);
            return "Fazenda com ID " + id + " foi deletada com sucesso.";
        } catch (DataIntegrityViolationException e) {
            // Trata possíveis violações de integridade do banco de dados durante a deleção (por exemplo, se outras entidades referenciam esta fazenda)
            throw new DatabaseException("Não é possível deletar a fazenda com ID " + id + " porque ela possui relacionamentos com outras entidades.");
        }
    }
}