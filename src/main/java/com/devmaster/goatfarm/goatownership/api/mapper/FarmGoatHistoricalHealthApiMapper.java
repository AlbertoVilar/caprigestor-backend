package com.devmaster.goatfarm.goatownership.api.mapper;

import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalHealthEventDTO;
import com.devmaster.goatfarm.goatownership.api.dto.FarmGoatHistoricalHealthResponseDTO;
import com.devmaster.goatfarm.goatownership.application.model.FarmGoatHistoricalHealthSnapshot;
import com.devmaster.goatfarm.goatownership.application.model.HistoricalHealthEventItem;
import org.springframework.stereotype.Component;

/** Maps neutral historical health models to the registry transport contract. */
@Component
public class FarmGoatHistoricalHealthApiMapper {

    public FarmGoatHistoricalHealthResponseDTO toResponse(FarmGoatHistoricalHealthSnapshot snapshot) {
        if (snapshot == null) {
            return null;
        }
        return new FarmGoatHistoricalHealthResponseDTO(
                snapshot.goatId().value(),
                snapshot.events().stream().map(this::toEventDto).toList()
        );
    }

    private FarmGoatHistoricalHealthEventDTO toEventDto(HistoricalHealthEventItem item) {
        return new FarmGoatHistoricalHealthEventDTO(
                item.id(), item.farmId(), item.type(), item.status(), item.title(), item.description(),
                item.scheduledDate(), item.performedAt(), item.responsible(), item.notes(), item.productName(),
                item.activeIngredient(), item.dose(), item.doseUnit(), item.route(), item.batchNumber(),
                item.withdrawalMilkDays(), item.withdrawalMeatDays(), item.milkWithdrawalEndDate(), item.meatWithdrawalEndDate()
        );
    }
}
