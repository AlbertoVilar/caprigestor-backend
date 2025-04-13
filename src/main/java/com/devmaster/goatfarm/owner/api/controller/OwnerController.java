package com.devmaster.goatfarm.owner.api.controller;

import com.devmaster.goatfarm.owner.business.bo.OwnerRequestVO;
import com.devmaster.goatfarm.owner.business.bo.OwnerResponseVO;
import com.devmaster.goatfarm.owner.facade.OwnerFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/owners")
public class OwnerController {

    @Autowired
    private OwnerFacade ownerFacade;

    @PostMapping
    public OwnerResponseVO createOwner(@RequestBody OwnerRequestVO requestVO) {
        if (requestVO == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados do proprietário não podem ser nulos.");
        }
        return ownerFacade.createOwner(requestVO);
    }

    @PutMapping("/{id}")
    public OwnerResponseVO updateOwner(@PathVariable Long id,
                                       @RequestBody OwnerRequestVO requestVO) {
        if (requestVO == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dados para atualização não podem ser nulos.");
        }
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
    public ResponseEntity<String> deleteOwner(@PathVariable Long id) {
        ownerFacade.deleteOwner(id);
        return ResponseEntity.ok("Proprietário com ID " + id + " foi deletado com sucesso.");
    }
}
