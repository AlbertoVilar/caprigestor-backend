package com.devmaster.goatfarm.goat.persistence.repository;

import com.devmaster.goatfarm.goat.persistence.entity.GoatEntity;
import com.devmaster.goatfarm.goat.enums.Gender;
import com.devmaster.goatfarm.goat.enums.GoatBreed;
import com.devmaster.goatfarm.goat.enums.GoatStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface GoatRepository extends JpaRepository<GoatEntity, String> {

    Optional<GoatEntity> findByRegistrationNumber(String registrationNumber);

    @Query("SELECT g FROM GoatEntity g WHERE g.registrationNumber = :id AND g.farm.id = :farmId")
    Optional<GoatEntity> findByIdAndFarmId(@Param("id") String id, @Param("farmId") Long farmId);

    @Query("SELECT g FROM GoatEntity g WHERE g.technicalId = :technicalId")
    Optional<GoatEntity> findByTechnicalId(@Param("technicalId") Long technicalId);

    @Query("SELECT g FROM GoatEntity g WHERE g.technicalId = :technicalId AND g.farm.id = :farmId")
    Optional<GoatEntity> findByTechnicalIdAndFarmId(@Param("technicalId") Long technicalId, @Param("farmId") Long farmId);

    Page<GoatEntity> findAllByFarmId(Long farmId, Pageable pageable);

    Page<GoatEntity> findAllByFarmIdAndBreed(Long farmId, GoatBreed breed, Pageable pageable);

    long countByFarmId(Long farmId);

    long countByFarmIdAndGender(Long farmId, Gender gender);

    long countByFarmIdAndStatus(Long farmId, GoatStatus status);

    @Query("SELECT COUNT(g) FROM GoatEntity g WHERE g.farm.id = :farmId AND g.breed IS NULL")
    long countByFarmIdWithoutBreed(@Param("farmId") Long farmId);

    @Query("SELECT g.breed AS breed, COUNT(g) AS total FROM GoatEntity g WHERE g.farm.id = :farmId AND g.breed IS NOT NULL GROUP BY g.breed ORDER BY COUNT(g) DESC, g.breed ASC")
    java.util.List<GoatBreedCountProjection> countBreedsByFarmId(@Param("farmId") Long farmId);

    @Query("SELECT g FROM GoatEntity g WHERE g.farm.id = :farmId AND LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<GoatEntity> findByNameAndFarmId(@Param("farmId") Long farmId, @Param("name") String name, Pageable pageable);

    @Query("SELECT g FROM GoatEntity g WHERE g.farm.id = :farmId AND LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%')) AND g.breed = :breed")
    Page<GoatEntity> findByNameAndFarmIdAndBreed(@Param("farmId") Long farmId, @Param("name") String name, @Param("breed") GoatBreed breed, Pageable pageable);

    @Query("""
            SELECT g
            FROM GoatEntity g
            WHERE g.farm.id = :farmId
              AND (
                  g.mother.registrationNumber = :parentRegistrationNumber
                  OR g.father.registrationNumber = :parentRegistrationNumber
              )
            ORDER BY CASE WHEN g.birthDate IS NULL THEN 1 ELSE 0 END, g.birthDate DESC, g.registrationNumber ASC
            """)
    List<GoatEntity> findOffspringByParentRegistration(@Param("farmId") Long farmId, @Param("parentRegistrationNumber") String parentRegistrationNumber);

    @Query("SELECT g FROM GoatEntity g WHERE g.farm.id = :farmId AND (g.fatherTechnicalId = :parentId OR g.motherTechnicalId = :parentId) ORDER BY g.birthDate DESC, g.registrationNumber ASC")
    List<GoatEntity> findOffspringByParentTechnicalId(@Param("farmId") Long farmId, @Param("parentId") Long parentId);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM cabras WHERE capril_id IN (SELECT c.id FROM capril c WHERE c.user_id != :adminId)")
    void deleteGoatsFromOtherUsers(@Param("adminId") Long adminId);

    // Carregamento completo pelo grafo técnico de genealogia (pai/mãe, avós e bisavós).
    // A rota v1 continua usando RG como lookup explícito; os joins internos não o usam como FK.
    @Query(
        "SELECT g FROM GoatEntity g " +
        "LEFT JOIN FETCH g.farm gf " +
        "LEFT JOIN FETCH gf.user " +
        "LEFT JOIN FETCH g.user " +
        "LEFT JOIN FETCH g.technicalFather f " +
        "LEFT JOIN FETCH f.technicalFather ff " +
        "LEFT JOIN FETCH ff.technicalFather fff " +
        "LEFT JOIN FETCH ff.technicalMother ffm " +
        "LEFT JOIN FETCH f.technicalMother fm " +
        "LEFT JOIN FETCH fm.technicalFather fmf " +
        "LEFT JOIN FETCH fm.technicalMother fmm " +
        "LEFT JOIN FETCH g.technicalMother m " +
        "LEFT JOIN FETCH m.technicalFather mf " +
        "LEFT JOIN FETCH mf.technicalFather mff " +
        "LEFT JOIN FETCH mf.technicalMother mfm " +
        "LEFT JOIN FETCH m.technicalMother mm " +
        "LEFT JOIN FETCH mm.technicalFather mmf " +
        "LEFT JOIN FETCH mm.technicalMother mmm " +
        "WHERE g.registrationNumber = :registrationNumber AND g.farm.id = :farmId"
    )
    Optional<GoatEntity> findByRegistrationNumberAndFarmIdWithTechnicalFamilyGraph(
            @Param("registrationNumber") String registrationNumber,
            @Param("farmId") Long farmId
    );
}
