package com.devmaster.goatfarm.application.ports.in;

public interface LactationCommandUseCase {

    void dryLactation(Long farmId, String goatId, Long lactationId);
}
