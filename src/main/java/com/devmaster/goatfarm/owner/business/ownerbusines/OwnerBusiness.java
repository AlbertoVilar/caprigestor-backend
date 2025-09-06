package com.devmaster.goatfarm.owner.business.ownerbusines;

import com.devmaster.goatfarm.owner.business.bo.OwnerRequestVO;
import com.devmaster.goatfarm.owner.business.bo.OwnerResponseVO;
import com.devmaster.goatfarm.owner.dao.OwnerDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OwnerBusiness {

    @Autowired
    private OwnerDAO ownerDAO;

    public OwnerResponseVO createOwner(OwnerRequestVO requestVO) {

       return ownerDAO.createOwner(requestVO);
    }

    public OwnerResponseVO updateGoatOwner(Long id, OwnerRequestVO requestVO) {

        return ownerDAO.updateGoatOwner(id,requestVO);
    }


    public OwnerResponseVO findOwnerById(Long id) {

        return ownerDAO.findOwnerById(id);
    }

    public OwnerResponseVO findOwnerByUserId(Long userId) {

        return ownerDAO.findOwnerByUserId(userId);
    }

    public Page<OwnerResponseVO> findAllOwners(Pageable pageable) {

        return ownerDAO.findAllOwners(pageable);
    }

    public Page<OwnerResponseVO> searchOwnerByName(String name, Pageable pageable) {

        return ownerDAO.searchOwnerByName(name, pageable);
    }

    public String deleteOwner(Long id) {

        return ownerDAO.deleteOwner(id);
    }
}
