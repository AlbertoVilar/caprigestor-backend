package com.devmaster.goatfarm.goat.model.repository;

import com.devmaster.goatfarm.goat.model.entity.Goat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface GoatRepository extends JpaRepository<Goat, String> {

    Optional<Goat> findByRegistrationNumber(String registrationNumber);

    @Query("SELECT g FROM Goat g WHERE g.id = :id AND g.farm.id = :farmId")
    Optional<Goat> findByIdAndFarmId(@Param("id") String id, @Param("farmId") Long farmId);

    Page<Goat> findAllByFarmId(Long farmId, Pageable pageable);

    @Query("SELECT g FROM Goat g WHERE g.farm.id = :farmId AND LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Goat> findByNameAndFarmId(@Param("farmId") Long farmId, @Param("name") String name, Pageable pageable);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "DELETE FROM cabras WHERE capril_id IN (SELECT c.id FROM capril c WHERE c.user_id != :adminId)")
    void deleteGoatsFromOtherUsers(@Param("adminId") Long adminId);

    // Carregamento completo com JOIN FETCH para genealogia (pai/mãe, avós e bisavós)
    @Query(
        "SELECT g FROM Goat g " +
        "LEFT JOIN FETCH g.farm gf " +
        "LEFT JOIN FETCH gf.user " +
        "LEFT JOIN FETCH g.father f " +
        "LEFT JOIN FETCH f.father ff " +
        "LEFT JOIN FETCH ff.father fff " +
        "LEFT JOIN FETCH ff.mother ffm " +
        "LEFT JOIN FETCH f.mother fm " +
        "LEFT JOIN FETCH fm.father fmf " +
        "LEFT JOIN FETCH fm.mother fmm " +
        "LEFT JOIN FETCH g.mother m " +
        "LEFT JOIN FETCH m.father mf " +
        "LEFT JOIN FETCH mf.father mff " +
        "LEFT JOIN FETCH mf.mother mfm " +
        "LEFT JOIN FETCH m.mother mm " +
        "LEFT JOIN FETCH mm.father mmf " +
        "LEFT JOIN FETCH mm.mother mmm " +
        "WHERE g.registrationNumber = :id AND g.farm.id = :farmId"
    )
    Optional<Goat> findByIdAndFarmIdWithFamilyGraph(@Param("id") String id, @Param("farmId") Long farmId);
}

