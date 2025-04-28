package com.devmaster.goatfarm.owner.facade;

import com.devmaster.goatfarm.owner.business.bo.OwnerRequestVO;
import com.devmaster.goatfarm.owner.business.bo.OwnerResponseVO;
import com.devmaster.goatfarm.owner.business.ownerbusines.OwnerBusiness;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OwnerFacade {

    @Autowired
    private OwnerBusiness ownerBusiness;

    public OwnerResponseVO createOwner(OwnerRequestVO requestVO) {

        return ownerBusiness.createOwner(requestVO);
    }

    public OwnerResponseVO updateGoatOwner(Long id, OwnerRequestVO requestVO) {

        return ownerBusiness.updateGoatOwner(id,requestVO);
    }


    public OwnerResponseVO findOwnerById(Long id) {

        return ownerBusiness.findOwnerById(id);
    }
    public Page<OwnerResponseVO> searchOwnerByName(String name , Pageable pageable) {

        return ownerBusiness.searchOwnerByName(name, pageable);
    }

    public Page<OwnerResponseVO> findAllOwners(Pageable pageable) {

        return ownerBusiness.findAllOwners(pageable);
    }

    public String deleteOwner(Long id) {

        return ownerBusiness.deleteOwner(id);
    }
}
