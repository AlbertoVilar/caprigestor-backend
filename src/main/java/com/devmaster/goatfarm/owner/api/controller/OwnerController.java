package com.devmaster.goatfarm.owner.api.controller;

import com.devmaster.goatfarm.owner.business.bo.OwnerRequestVO;
import com.devmaster.goatfarm.owner.business.bo.OwnerResponseVO;
import com.devmaster.goatfarm.owner.facade.OwnerFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/owners")
public class OwnerController {

    @Autowired
    private OwnerFacade ownerFacade;

    @PostMapping
    public OwnerResponseVO createOwner(@RequestBody OwnerRequestVO requestVO) {
        return ownerFacade.createOwner(requestVO);
    }

    @PutMapping("/{id}")
    public OwnerResponseVO updateOwner(@PathVariable Long id,
                                       @RequestBody OwnerRequestVO requestVO) {
        return ownerFacade.updateGoatOwner(id, requestVO);
    }

    @GetMapping("/{id}")
    public OwnerResponseVO getOwnerById(@PathVariable Long id) {
        return ownerFacade.findOwnerById(id);
    }

    @GetMapping
    public List<OwnerResponseVO> getAllOwners() {
        return ownerFacade.findAllOwners();
    }

    @DeleteMapping("/{id}")
    public String deleteOwner(@PathVariable Long id) {
        return ownerFacade.deleteOwner(id);
    }
}
