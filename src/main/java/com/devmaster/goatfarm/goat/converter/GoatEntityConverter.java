package com.devmaster.goatfarm.goat.converter;

import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.authority.model.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class GoatEntityConverter {


    public static Goat toEntity(GoatRequestVO requestVO,
                                Goat father, Goat mother,
                                User user,  // Incluindo o usuário como parâmetro
                                GoatFarm goatFarm  // Garantir que goatFarm seja passado corretamente
                               ) {

        Goat goat = new Goat();
        goat.setRegistrationNumber(requestVO.getRegistrationNumber());
        goat.setName(requestVO.getName());
        goat.setGender(requestVO.getGender());
        goat.setBreed(requestVO.getBreed());
        goat.setColor(requestVO.getColor());
        goat.setBirthDate(requestVO.getBirthDate());
        goat.setStatus(requestVO.getStatus());
        goat.setTod(requestVO.getTod());
        goat.setToe(requestVO.getToe());
        goat.setCategory(requestVO.getCategory());
        goat.setFather(father);
        goat.setMother(mother);
        goat.setUser(user);
        goat.setFarm(goatFarm);
        
        return goat;
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

                goat.getUser() != null ? goat.getUser().getName() : null, // Incluindo o nome do usuário

                goat.getFarm() != null ? goat.getFarm().getId() : null,
                goat.getFarm() != null ? goat.getFarm().getName() : null

        );
    }

}
