package com.devmaster.goatfarm.goat.dao;

import com.devmaster.goatfarm.goat.business.bo.GoatRequestVO;
import com.devmaster.goatfarm.goat.business.bo.GoatResponseVO;
import com.devmaster.goatfarm.goat.mapper.GoatMapper;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.farm.model.entity.GoatFarm;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.Optional;

@Component
public class GoatDAO {

    private final EntityManager entityManager;
    private final GoatMapper goatMapper;
    private final GoatRepository goatRepository;

    @Autowired
    public GoatDAO(EntityManager entityManager, GoatMapper goatMapper, GoatRepository goatRepository) {
        this.entityManager = entityManager;
        this.goatMapper = goatMapper;
        this.goatRepository = goatRepository;
    }

    @Transactional
    public Goat save(Goat goat) {
        return goatRepository.save(goat);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public boolean existsById(String registrationNumber) {
        return goatRepository.existsById(registrationNumber);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<Goat> findByRegistrationNumber(String registrationNumber) {
        return goatRepository.findByRegistrationNumber(registrationNumber);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<Goat> findAll(Pageable pageable) {
        return goatRepository.findAll(pageable);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<Goat> searchGoatByName(String name, Pageable pageable) {
        return goatRepository.searchGoatByName(name, pageable);
    }

    @Transactional(Transactional.TxType.SUPPORTS)
    public Page<Goat> findByNameAndFarmId(Long farmId, String name, Pageable pageable) {
        return goatRepository.findByNameAndFarmId(farmId, name, pageable);
    }

    @Transactional
    public GoatResponseVO saveGoat(Goat goat) {
        entityManager.persist(goat);
        entityManager.flush();
        return goatMapper.toResponseVO(goat);
    }

    public GoatResponseVO findGoatById(String registrationNumber) {
        TypedQuery<Goat> query = entityManager.createQuery(
                "SELECT g FROM Goat g WHERE g.registrationNumber = :registrationNumber",
                Goat.class
        );
        query.setParameter("registrationNumber", registrationNumber);
        Goat goat = query.getResultStream().findFirst().orElse(null);
        return Optional.ofNullable(goat).map(goatMapper::toResponseVO).orElse(null);
    }

    public Page<Goat> searchGoatByNameAndFarmId(String goatName, Long farmId, Pageable pageable) {
        TypedQuery<Goat> query = entityManager.createQuery(
                "SELECT g FROM Goat g WHERE (:goatName IS NULL OR LOWER(g.name) LIKE LOWER(CONCAT('%', :goatName, '%'))) " +
                "AND (:farmId IS NULL OR g.farm.id = :farmId)",
                Goat.class
        );
        query.setParameter("goatName", goatName);
        query.setParameter("farmId", farmId);
        return PageableExecutionUtils.getPage(query.getResultList(), pageable, () -> countGoats(goatName, farmId));
    }

    public Page<Goat> findByFarmIdAndOptionalRegistrationNumber(Long farmId, String registrationNumber, Pageable pageable) {
        String jpql = "SELECT g FROM Goat g WHERE g.farm.id = :farmId" +
                      " AND (:registrationNumber IS NULL OR g.registrationNumber = :registrationNumber)";
        TypedQuery<Goat> query = entityManager.createQuery(jpql, Goat.class);
        query.setParameter("farmId", farmId);
        query.setParameter("registrationNumber", registrationNumber);
        return PageableExecutionUtils.getPage(query.getResultList(), pageable, () -> countGoatsByFarmIdAndRegistration(farmId, registrationNumber));
    }

    @Transactional
    public GoatResponseVO updateGoat(String registrationNumber, GoatRequestVO requestVO, GoatFarm farm, Goat father, Goat mother) {
        TypedQuery<Goat> query = entityManager.createQuery(
                "SELECT g FROM Goat g WHERE g.registrationNumber = :registrationNumber",
                Goat.class
        );
        query.setParameter("registrationNumber", registrationNumber);
        Goat goat = query.getResultStream().findFirst().orElseThrow(() -> new IllegalArgumentException("Cabra nÃ£o encontrada para atualizaÃ§Ã£o"));

                if (requestVO.getName() != null) goat.setName(requestVO.getName());
        if (requestVO.getRegistrationNumber() != null) goat.setRegistrationNumber(requestVO.getRegistrationNumber());
        if (farm != null) goat.setFarm(farm);
        if (father != null) goat.setFather(father);
        if (mother != null) goat.setMother(mother);

        entityManager.merge(goat);
        entityManager.flush();
        return goatMapper.toResponseVO(goat);
    }

    public void deleteGoat(String registrationNumber) {
        TypedQuery<Goat> query = entityManager.createQuery(
                "SELECT g FROM Goat g WHERE g.registrationNumber = :registrationNumber",
                Goat.class
        );
        query.setParameter("registrationNumber", registrationNumber);
        Goat goat = query.getResultStream().findFirst().orElse(null);
        if (goat != null) {
            entityManager.remove(goat);
            entityManager.flush();
        }
    }

    private long countGoats(String goatName, Long farmId) {
        String jpql = "SELECT COUNT(g) FROM Goat g WHERE (:goatName IS NULL OR LOWER(g.name) LIKE LOWER(CONCAT('%', :goatName, '%'))) " +
                      "AND (:farmId IS NULL OR g.farm.id = :farmId)";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("goatName", goatName);
        query.setParameter("farmId", farmId);
        return query.getSingleResult();
    }

    private long countGoatsByFarmIdAndRegistration(Long farmId, String registrationNumber) {
        String jpql = "SELECT COUNT(g) FROM Goat g WHERE g.farm.id = :farmId" +
                      " AND (:registrationNumber IS NULL OR g.registrationNumber = :registrationNumber)";
        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("farmId", farmId);
        query.setParameter("registrationNumber", registrationNumber);
        return query.getSingleResult();
    }

    @Transactional
    public void deleteGoatsFromOtherUsers(Long adminId) {
        goatRepository.deleteGoatsFromOtherUsers(adminId);
    }
}
