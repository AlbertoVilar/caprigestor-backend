package com.devmaster.goatfarm.application.core.business.validation;

import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
import org.springframework.stereotype.Service;

@Service
public class GoatGenderValidator {

    private final GoatPersistencePort goatPersistencePort;

    public GoatGenderValidator(GoatPersistencePort goatPersistencePort) {
        this.goatPersistencePort = goatPersistencePort;
    }

    public Goat requireFemale(Long farmId, String goatId) {
        Goat goat = requireGoat(farmId, goatId);
        if (goat.getGender() != Gender.FEMEA) {
            throw new BusinessRuleException("gender", "Apenas fêmeas podem ter lactação.");
        }
        return goat;
    }

    public Goat requireActive(Long farmId, String goatId) {
        Goat goat = requireGoat(farmId, goatId);
        validateActiveStatus(goat);
        return goat;
    }

    public Goat requireFemaleAndActive(Long farmId, String goatId) {
        Goat goat = requireFemale(farmId, goatId);
        validateActiveStatus(goat);
        return goat;
    }

    public void requireGender(
            Gender actualGender,
            Gender expectedGender,
            String fieldName,
            String parentArticle,
            String parentRole
    ) {
        if (actualGender == expectedGender) {
            return;
        }

        if (actualGender == null) {
            throw new BusinessRuleException(fieldName, "O sexo do animal informado para " + parentArticle + " " + parentRole + " é obrigatório.");
        }

        String actualGenderDescription = actualGender == Gender.FEMEA ? "feminino" : "masculino";
        throw new BusinessRuleException(
                fieldName,
                "O registro informado para " + parentArticle + " " + parentRole
                        + " corresponde a um animal do sexo " + actualGenderDescription + "."
        );
    }

    private Goat requireGoat(Long farmId, String goatId) {
        return goatPersistencePort.findByIdAndFarmId(goatId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada para a fazenda informada."));
    }

    private void validateActiveStatus(Goat goat) {
        if (goat.getStatus() != GoatStatus.ATIVO) {
            throw new BusinessRuleException(
                    "status",
                    "Apenas cabras com status ATIVO podem ser manipuladas. Status atual: " + goat.getStatus()
            );
        }
    }
}
