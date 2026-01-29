package com.devmaster.goatfarm.application.core.business.validation;

import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationError;
import com.devmaster.goatfarm.config.exceptions.custom.ValidationException;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.persistence.entity.Goat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class GoatGenderValidator {

    private final GoatPersistencePort goatPersistencePort;

    public GoatGenderValidator(GoatPersistencePort goatPersistencePort) {
        this.goatPersistencePort = goatPersistencePort;
    }

    public Goat requireFemale(Long farmId, String goatId) {
        Goat goat = goatPersistencePort.findByIdAndFarmId(goatId, farmId)
                .orElseThrow(() -> new ResourceNotFoundException("Cabra não encontrada para a fazenda informada."));

        if (goat.getGender() != Gender.FEMEA) {
            ValidationError err = new ValidationError(Instant.now(), HttpStatus.UNPROCESSABLE_ENTITY.value(), "Erro de validação");
            err.addError("gender", "Apenas fêmeas podem ter lactação.");
            throw new ValidationException(err);
        }

        return goat;
    }
}
