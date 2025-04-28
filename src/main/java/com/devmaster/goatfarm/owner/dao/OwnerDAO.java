package com.devmaster.goatfarm.owner.dao;

import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.DuplicateEntityException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.owner.business.bo.OwnerRequestVO;
import com.devmaster.goatfarm.owner.business.bo.OwnerResponseVO;
import com.devmaster.goatfarm.owner.converter.OwnerEntityConverter;
import com.devmaster.goatfarm.owner.model.entity.Owner;
import com.devmaster.goatfarm.owner.model.repository.OwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
public class OwnerDAO {

    @Autowired
    private OwnerRepository ownerRepository;

    @Transactional
    public OwnerResponseVO createOwner(OwnerRequestVO requestVO) {
        if (requestVO == null) {
            throw new IllegalArgumentException("Os dados do proprietário para criação não podem ser nulos.");
        }

        if (ownerRepository.existsByCpf(requestVO.getCpf())) {
            throw new DuplicateEntityException("Já existe um proprietário com o CPF '" + requestVO.getCpf() + "'.");
        }

        if (ownerRepository.existsByEmail(requestVO.getEmail())) {
            throw new DuplicateEntityException("Já existe um proprietário com o email '" + requestVO.getEmail() + "'.");
        }

        Owner owner = OwnerEntityConverter.toEntity(requestVO);
        try {
            owner = ownerRepository.save(owner);
            return OwnerEntityConverter.toVO(owner);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Erro ao salvar o proprietário: " + e.getMessage());
        }
    }

    @Transactional
    public OwnerResponseVO updateGoatOwner(Long id, OwnerRequestVO requestVO) {
        Owner ownerToUpdate = ownerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proprietário com ID " + id + " não encontrado."));

        if (!ownerToUpdate.getCpf().equals(requestVO.getCpf()) && ownerRepository.existsByCpf(requestVO.getCpf())) {
            throw new DuplicateEntityException("Já existe outro proprietário com o CPF '" + requestVO.getCpf() + "'.");
        }

        if (!ownerToUpdate.getEmail().equals(requestVO.getEmail()) && ownerRepository.existsByEmail(requestVO.getEmail())) {
            throw new DuplicateEntityException("Já existe outro proprietário com o email '" + requestVO.getEmail() + "'.");
        }

        OwnerEntityConverter.entityUpdate(ownerToUpdate, requestVO);
        try {
            return OwnerEntityConverter.toVO(ownerRepository.save(ownerToUpdate));
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Erro ao atualizar o proprietário com ID " + id + ": " + e.getMessage());
        }
    }

    @Transactional
    public OwnerResponseVO findOwnerById(Long id) {
        Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proprietário com ID " + id + " não encontrado."));
        return OwnerEntityConverter.toVO(owner);
    }

    @Transactional(readOnly = true)
    public Page<OwnerResponseVO> searchOwnerByName(String name, Pageable pageable) {
        System.out.println("Buscando proprietários com nome: '" + name + "'"); // Adicione esta linha
        Page<Owner> resultOwners = ownerRepository.searchOwnerByName(name, pageable);
        return resultOwners.map(OwnerEntityConverter::toVO);
    }

    @Transactional
    public Page<OwnerResponseVO> findAllOwners(Pageable pageable) {
        Page<Owner> resultOwners = ownerRepository.findAll(pageable);
        return resultOwners.map(OwnerEntityConverter::toVO);

    }

    @Transactional
    public String deleteOwner(Long id) {
        if (!ownerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Proprietário com ID " + id + " não encontrado.");
        }
        try {
            ownerRepository.deleteById(id);
            return "Proprietário com ID " + id + " foi deletado com sucesso.";
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Não é possível deletar o proprietário com ID " + id + " porque ele possui relacionamentos com outras entidades.");
        }
    }
}