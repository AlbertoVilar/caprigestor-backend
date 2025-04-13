package com.devmaster.goatfarm.owner.dao;



import com.devmaster.goatfarm.owner.business.bo.OwnerRequestVO;
import com.devmaster.goatfarm.owner.business.bo.OwnerResponseVO;
import com.devmaster.goatfarm.owner.converter.OwnerEntityConverter;
import com.devmaster.goatfarm.owner.model.entity.Owner;
import com.devmaster.goatfarm.owner.model.repository.OwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OwnerDAO {

    @Autowired
    private OwnerRepository ownerRepository;

    public OwnerResponseVO createOwner(OwnerRequestVO requestVO) {

        if (requestVO == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Owner request cannot be null");
        }

        Owner owner = OwnerEntityConverter.toEntity(requestVO);
        owner = ownerRepository.save(owner);
        return OwnerEntityConverter.toVO(owner);

    }
    public OwnerResponseVO updateGoatOwner(Long id, OwnerRequestVO requestVO) {
       Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Elemento não encontrado " + id));

        OwnerEntityConverter.entityUpdate(owner, requestVO);

        return OwnerEntityConverter.toVO(ownerRepository.save(owner));
    }


    public OwnerResponseVO findOwnerById(Long id) {

             Owner owner = ownerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Elemento não encontrado " + id));
       return OwnerEntityConverter.toVO(owner);
    }

    public List<OwnerResponseVO> findAllOwners() {

        List<Owner> resultOwners = ownerRepository.findAll();

        return resultOwners.stream()
                .map(OwnerEntityConverter::toVO).collect(Collectors.toList());
    }

    public String deleteOwner(Long id) {
        if (!ownerRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Elemento não encontrado " + id);
        }
        ownerRepository.deleteById(id);
        return "Goat Farm com ID " + id + " foi deletada com sucesso.";
    }

}
