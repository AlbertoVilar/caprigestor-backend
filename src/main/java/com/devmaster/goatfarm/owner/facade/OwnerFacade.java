package com.devmaster.goatfarm.owner.facade;

import com.devmaster.goatfarm.owner.business.bo.OwnerRequestVO;
import com.devmaster.goatfarm.owner.business.bo.OwnerResponseVO;
import com.devmaster.goatfarm.owner.business.ownerbusines.OwnerBusiness;
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<OwnerResponseVO> findAllOwners() {

        return ownerBusiness.findAllOwners();
    }

    public String deleteOwner(Long id) {

        return ownerBusiness.deleteOwner(id);
    }
}
