package com.devmaster.goatfarm.reproduction.business.reproductionservice;

import com.devmaster.goatfarm.application.core.business.validation.GoatGenderValidator;
import com.devmaster.goatfarm.farm.application.ports.out.GoatFarmPersistencePort;
import com.devmaster.goatfarm.goat.application.ports.in.GoatManagementUseCase;
import com.devmaster.goatfarm.goat.application.ports.out.GoatPersistencePort;
import com.devmaster.goatfarm.goat.application.routing.GoatReferenceResolver;
import com.devmaster.goatfarm.reproduction.application.ports.in.ReproductionQueryUseCase;
import com.devmaster.goatfarm.reproduction.application.ports.out.PregnancyPersistencePort;
import com.devmaster.goatfarm.reproduction.application.ports.out.ReproductiveEventPersistencePort;
import com.devmaster.goatfarm.reproduction.business.bo.*;
import com.devmaster.goatfarm.reproduction.business.mapper.ReproductionBusinessMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.Clock;
import java.time.LocalDate;

/** Test fixture retained while the legacy behavior assertions are redistributed. */
class LegacyReproductionTestFacade implements ReproductionQueryUseCase {
    private final BreedingCommandBusiness breeding;
    private final PregnancyCommandBusiness pregnancy;
    private final BirthCommandBusiness birth;
    private final WeaningCommandBusiness weaning;
    private final ReproductionQueryBusiness query;

    LegacyReproductionTestFacade(PregnancyPersistencePort pregnancyPort, ReproductiveEventPersistencePort eventPort,
                         GoatPersistencePort goatPort, GoatReferenceResolver resolver, GoatFarmPersistencePort farmPort,
                         GoatManagementUseCase goatManagement, GoatGenderValidator validator, ReproductionBusinessMapper mapper,
                         Clock clock) {
        EffectiveCoverageDateResolver dateResolver = new EffectiveCoverageDateResolver(eventPort);
        breeding = new BreedingCommandBusiness(pregnancyPort, eventPort, validator, mapper, dateResolver, clock);
        pregnancy = new PregnancyCommandBusiness(pregnancyPort, eventPort, validator, mapper, dateResolver, clock);
        birth = new BirthCommandBusiness(pregnancyPort, eventPort, goatPort, resolver, farmPort, goatManagement, validator, mapper, clock);
        weaning = new WeaningCommandBusiness(eventPort, goatPort, resolver, validator, mapper, clock);
        query = new ReproductionQueryBusiness(pregnancyPort, eventPort, validator, mapper, dateResolver, clock);
    }
    public ReproductiveEventResponseVO registerBreeding(Long f, String g, BreedingRequestVO v) { return breeding.registerBreeding(f,g,v); }
    public ReproductiveEventResponseVO correctCoverage(Long f, String g, Long id, CoverageCorrectionRequestVO v) { return breeding.correctCoverage(f,g,id,v); }
    public PregnancyResponseVO confirmPregnancy(Long f, String g, PregnancyConfirmRequestVO v) { return pregnancy.confirmPregnancy(f,g,v); }
    public ReproductiveEventResponseVO registerPregnancyCheck(Long f, String g, PregnancyCheckRequestVO v) { return pregnancy.registerPregnancyCheck(f,g,v); }
    public PregnancyResponseVO closePregnancy(Long f, String g, Long id, PregnancyCloseRequestVO v) { return pregnancy.closePregnancy(f,g,id,v); }
    public BirthResponseVO registerBirth(Long f, String g, Long id, BirthRequestVO v) { return birth.registerBirth(f,g,id,v); }
    public WeaningResponseVO registerWeaning(Long f, String g, WeaningRequestVO v) { return weaning.registerWeaning(f,g,v); }
    @Override public PregnancyResponseVO getActivePregnancy(Long f,String g){return query.getActivePregnancy(f,g);}
    @Override public PregnancyResponseVO getPregnancyById(Long f,String g,Long id){return query.getPregnancyById(f,g,id);}
    @Override public Page<PregnancyResponseVO> getPregnancies(Long f,String g,Pageable p){return query.getPregnancies(f,g,p);}
    @Override public Page<ReproductiveEventResponseVO> getReproductiveEvents(Long f,String g,Pageable p){return query.getReproductiveEvents(f,g,p);}
    @Override public DiagnosisRecommendationResponseVO getDiagnosisRecommendation(Long f,String g,LocalDate d){return query.getDiagnosisRecommendation(f,g,d);}
    @Override public Page<PregnancyDiagnosisAlertVO> getPendingPregnancyDiagnosisAlerts(Long f,LocalDate d,Pageable p){return query.getPendingPregnancyDiagnosisAlerts(f,d,p);}
    @Override public Page<PregnancyDueAlertVO> getPendingBirthAlerts(Long f,LocalDate d,Pageable p){return query.getPendingBirthAlerts(f,d,p);}
}
