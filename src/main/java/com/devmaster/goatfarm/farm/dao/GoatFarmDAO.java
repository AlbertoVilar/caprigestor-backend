package com.devmaster.goatfarm.farm.dao;

import com.devmaster.goatfarm.farm.business.bo.GoatFarmRequestVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import com.devmaster.goatfarm.farm.converter.GoatFarmEntityConverter;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.farm.model.repository.GoatFarmRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GoatFarmDAO {

    @Autowired
    private GoatFarmRepository goatFarmRepository;

    public GoatFarmResponseVO createGoatFarm(GoatFarmRequestVO requestVO) {

        if (requestVO != null) {

            GoatFarm goatFarm = GoatFarmEntityConverter.toEntity(requestVO);
            goatFarm = goatFarmRepository.save(goatFarm);

            return GoatFarmEntityConverter.toVO(goatFarm);

        } else {
            return null;
        }

    }
    public GoatFarmResponseVO updateGoatFarm(Long id, GoatFarmRequestVO requestVO) {
        GoatFarm goatFarmUpdated = goatFarmRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Elemento não encontrado " + id));

        GoatFarmEntityConverter.entityUpdate(goatFarmUpdated, requestVO);

        return GoatFarmEntityConverter.toVO(goatFarmRepository.save(goatFarmUpdated));
    }


    public GoatFarmResponseVO findGoatFarmById(Long id) {

              GoatFarm goatFarm = goatFarmRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Elemento não encontrado " + id));
       return GoatFarmEntityConverter.toVO(goatFarm);
    }

    public List<GoatFarmResponseVO> findAllGoatFarm() {

        List<GoatFarm> resultGoatFarms = goatFarmRepository.findAll();

        return resultGoatFarms.stream()
                .map(GoatFarmEntityConverter::toVO).collect(Collectors.toList());
    }

    public String deleteGoatFarm(Long id) {
        if (!goatFarmRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Elemento não encontrado " + id);
        }
        goatFarmRepository.deleteById(id);
        return "Goat Farm com ID " + id + " foi deletada com sucesso.";
    }

}
