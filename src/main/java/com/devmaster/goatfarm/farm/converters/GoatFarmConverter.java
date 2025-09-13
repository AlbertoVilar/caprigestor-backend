package com.devmaster.goatfarm.farm.converters;

import com.devmaster.goatfarm.address.model.entity.Address;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.authority.model.entity.User;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GoatFarmConverter {

    public static GoatFarm toEntity(GoatFarmRequestVO requestVO, User user, Address address) {
        return new GoatFarm(
                null, // Passa null para o ID para que o banco de dados gere-o automaticamente
                requestVO.getName(),
                requestVO.getTod(),
                user,  // Passa o objeto User
                address // Passa o objeto Address
        );
    }

    public static void entityUpdate(GoatFarm goatFarm, GoatFarmRequestVO requestVO) {
        goatFarm.setId(requestVO.getId());
        goatFarm.setName(requestVO.getName());
        goatFarm.setTod(requestVO.getTod());
        // Não atualiza proprietário e endereço aqui, a lógica de atualização do relacionamento deve estar no DAO
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
        List<PhoneResponseVO> phones = entity.getPhones() != null ? 
                entity.getPhones().stream()
                        .map(p -> new PhoneResponseVO(p.getId(), p.getDdd(), p.getNumber()))
                        .collect(Collectors.toList()) : 
                List.of();

        GoatFarmFullResponseVO vo = new GoatFarmFullResponseVO();

        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setTod(entity.getTod());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());

        // Usuário
        if (entity.getUser() != null) {
            vo.setUserId(entity.getUser().getId());
            vo.setUserName(entity.getUser().getName());
            vo.setUserEmail(entity.getUser().getEmail());
            vo.setUserCpf(entity.getUser().getCpf());
        }

        // Endereço
        if (entity.getAddress() != null) {
            vo.setAddressId(entity.getAddress().getId());
            vo.setStreet(entity.getAddress().getStreet());
            vo.setDistrict(entity.getAddress().getNeighborhood()); // Se for 'district' no VO
            vo.setCity(entity.getAddress().getCity());
            vo.setState(entity.getAddress().getState());
            vo.setPostalCode(entity.getAddress().getPostalCode());
        }

        vo.setPhones(phones);

        return vo;
    }
}