package com.devmaster.goatfarm.farm.converters;

import com.devmaster.goatfarm.address.model.entity.Address;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.owner.model.entity.Owner;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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

    // Full ResponseVO
    public static GoatFarmFullResponseVO toFullVO(GoatFarm entity) {
        List<PhoneResponseVO> phones = entity.getPhones().stream()
                .map(p -> new PhoneResponseVO(p.getId(), p.getDdd(), p.getNumber()))
                .collect(Collectors.toList());

        GoatFarmFullResponseVO vo = new GoatFarmFullResponseVO();

        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setTod(entity.getTod());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());

        vo.setOwnerId(entity.getOwner().getId());
        vo.setOwnerName(entity.getOwner().getName());

        vo.setAddressId(entity.getAddress().getId());
        vo.setStreet(entity.getAddress().getStreet());
        vo.setDistrict(entity.getAddress().getNeighborhood()); // Se for 'district' no VO
        vo.setCity(entity.getAddress().getCity());
        vo.setState(entity.getAddress().getState());
        vo.setPostalCode(entity.getAddress().getPostalCode());
        vo.setPhones(phones);

        return vo;
    }


}
