package com.devmaster.goatfarm.goat.application.ports.out;

/** Read boundary for historical animal-sales data needed by farm summaries. */
public interface HistoricalAnimalSaleQueryPort {

    /**
     * Counts distinct goats with at least one completed, non-reversed sale by
     * the given seller farm.
     */
    long countDistinctSoldGoatsByFarmId(Long farmId);
}
