package com.devmaster.goatfarm.application.core.business.validation;

import com.devmaster.goatfarm.config.exceptions.custom.BusinessRuleException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.goat.application.ports.out.GoatValidationQueryPort;
import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import org.springframework.stereotype.Service;

@Service
public class GoatGenderValidator {

    private final GoatValidationQueryPort goatValidationQueryPort;

    public GoatGenderValidator(GoatValidationQueryPort goatValidationQueryPort) {
        this.goatValidationQueryPort = goatValidationQueryPort;
    }

    public void requireFemale(Long farmId, String goatId) {
        var goat = requireGoat(farmId, goatId);
        validateFemale(goat.gender());
    }

    public void requireActive(Long farmId, String goatId) {
        var goat = requireGoat(farmId, goatId);
        validateActiveStatus(goat.status());
    }

    public void requireFemaleAndActive(Long farmId, String goatId) {
        var goat = requireGoat(farmId, goatId);
        validateFemale(goat.gender());
        validateActiveStatus(goat.status());
    }

    /**
     * Validates sex by stable technical identity. Ownership is deliberately
     * checked by the caller so historical farm provenance is not mistaken for
     * the current operational farm.
     */
    public void requireFemale(GoatId goatId) {
        var goat = goatValidationQueryPort.findForValidation(goatId)
                .orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada para o GoatId informado."));
        validateFemale(goat.gender());
    }

    public void requireFemaleAndActive(GoatId goatId) {
        var goat = goatValidationQueryPort.findForValidation(goatId)
                .orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada para o GoatId informado."));
        validateFemale(goat.gender());
        validateActiveStatus(goat.status());
    }

    private void validateFemale(Gender gender) {
        if (gender != Gender.FEMEA) {
            throw new BusinessRuleException("gender", "Apenas fêmeas podem ter lactação.");
        }
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

    private GoatValidationQueryPort.GoatValidationSnapshot requireGoat(Long farmId, String goatId) {
        return goatValidationQueryPort.findForValidation(goatId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada para a fazenda informada."));
    }

    private void validateActiveStatus(GoatStatus status) {
        if (status != GoatStatus.ATIVO) {
            throw new BusinessRuleException(
                    "status",
                    "Apenas cabras com status ATIVO podem ser manipuladas. Status atual: " + status
            );
        }
    }
}
