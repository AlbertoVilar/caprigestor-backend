package com.devmaster.goatfarm.goatownership.application.ports.out;

import com.devmaster.goatfarm.goat.domain.GoatId;
import com.devmaster.goatfarm.goatownership.application.model.HistoricalHealthEventItem;

import java.util.List;

/**
 * Outbound port for retrieving historical health event facts.
 * Decouples the goatownership module from the health persistence implementation.
 */
public interface FarmGoatHistoricalHealthQueryPort {

    /**
     * Finds historical health events strictly attributed to the requesting farm and goat.
     *
     * @param goatId the structural technical identity of the goat
     * @param farmId the requesting farm identifier
     * @return list of historical health event facts
     */
    List<HistoricalHealthEventItem> findHistoricalHealthEvents(GoatId goatId, long farmId);
}
