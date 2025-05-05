package com.devmaster.goatfarm.farm.converter;

import com.devmaster.goatfarm.address.model.entity.Address;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.owner.model.entity.Owner;
import org.springframework.stereotype.Component;

@Component
public class GoatFarmConverter {

    public static GoatFarm toEntity(GoatFarmRequestVO requestVO, Owner owner, Address address) {
        return new GoatFarm(
                null, // Passa null para o ID para que o banco de dados gere-o automaticamente
                requestVO.getName(),
                requestVO.getTod(),
                owner,  // Passa o objeto Owner
                address // Passa o objeto Address
        );
    }

    public static void entityUpdate(GoatFarm goatFarm, GoatFarmRequestVO requestVO) {
        goatFarm.setId(requestVO.getId());
        goatFarm.setName(requestVO.getName());
        goatFarm.setTod(requestVO.getTod());
        // Não atualiza owner e address aqui, a lógica de atualização do relacionamento deve estar no DAO
    }

    public static GoatFarmResponseVO toVO(GoatFarm goatFarm) {
        return new GoatFarmResponseVO(
                goatFarm.getId(),
                goatFarm.getName(),
                goatFarm.getTod(),
                goatFarm.getCreatedAt(),
                goatFarm.getUpdatedAt()
        );
    }
}
