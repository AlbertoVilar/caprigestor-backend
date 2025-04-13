package com.devmaster.goatfarm.goat.converter;

import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.owner.model.entity.Owner;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class GoatEntityConverter {

    @Transactional
    public static Goat toEntity(GoatRequestVO requestVO,
                                Goat father, Goat mother,
                                Owner owner,  // Incluindo o owner como parâmetro
                                GoatFarm goatFarm  // Garantir que goatFarm seja passado corretamente
                               ) {

        return new Goat(
                requestVO.getRegistrationNumber(),
                requestVO.getName(),
                requestVO.getGender(),
                requestVO.getBreed(),
                requestVO.getColor(),
                requestVO.getBirthDate(),
                requestVO.getStatus(),
                requestVO.getTod(),
                requestVO.getToe(),
                requestVO.getCategory(),
                father,
                mother,
                owner, // Atribuindo o owner à cabra
                goatFarm

        );
    }


    @Transactional
    public static void updateGoatEntity(Goat goatToUpdate,
                                        GoatRequestVO requestVO,
                                        Goat father,
                                        Goat mother,
                                        GoatFarm goatFarm) {

        goatToUpdate.setName(requestVO.getName());
        goatToUpdate.setGender(requestVO.getGender());
        goatToUpdate.setBreed(requestVO.getBreed());
        goatToUpdate.setColor(requestVO.getColor());
        goatToUpdate.setBirthDate(requestVO.getBirthDate());
        goatToUpdate.setStatus(requestVO.getStatus());
        goatToUpdate.setTod(requestVO.getTod());
        goatToUpdate.setToe(requestVO.getToe());
        goatToUpdate.setCategory(requestVO.getCategory());
        goatToUpdate.setFather(father);
        goatToUpdate.setMother(mother);
        goatToUpdate.setFarm(goatFarm);
    }

    @Transactional
    public static GoatResponseVO toResponseVO(Goat goat) {
        return new GoatResponseVO(
                goat.getRegistrationNumber(),
                goat.getName(),
                goat.getGender(),
                goat.getBreed(),
                goat.getColor(),
                goat.getBirthDate(),
                goat.getStatus(),
                goat.getTod(),
                goat.getToe(),
                goat.getCategory(),

                goat.getFather() != null ? goat.getFather().getName() : null,
                goat.getFather() != null ? goat.getFather().getRegistrationNumber() : null,

                goat.getMother() != null ? goat.getMother().getName() : null,
                goat.getMother() != null ? goat.getMother().getRegistrationNumber() : null,

                goat.getOwner() != null ? goat.getOwner().getName() : null, // Incluindo o nome do proprietário

                goat.getFarm() != null ? goat.getFarm().getId() : null,
                goat.getFarm() != null ? goat.getFarm().getName() : null

        );
    }

}
