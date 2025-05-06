package com.devmaster.goatfarm.goat.model.repository;

import com.devmaster.goatfarm.goat.model.entity.Goat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GoatRepository extends JpaRepository<Goat, String> {

    @Query("""
        SELECT g FROM Goat g
        LEFT JOIN FETCH g.father
        LEFT JOIN FETCH g.mother
        LEFT JOIN FETCH g.farm
        LEFT JOIN FETCH g.owner
        WHERE g.registrationNumber = :registrationNumber
    """)
    Optional<Goat> findByRegistrationNumber(@Param("registrationNumber") String registrationNumber);

    Page<Goat> findAll(Pageable pageable);

    @Query("""
        SELECT g FROM Goat g
        WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    Page<Goat> searchGoatByName(@Param("name") String name, Pageable pageable);

    @Query("""
        SELECT g FROM Goat g
        LEFT JOIN FETCH g.father
        LEFT JOIN FETCH g.mother
        LEFT JOIN FETCH g.farm
        LEFT JOIN FETCH g.owner
        WHERE g.farm.id = :farmId
        AND (:registrationNumber IS NULL OR g.registrationNumber LIKE CONCAT('%', :registrationNumber, '%'))
    """)

    Page<Goat> findByFarmIdAndOptionalRegistrationNumber(
            @Param("farmId") Long farmId,
            @Param("registrationNumber") String registrationNumber,
            Pageable pageable
    );
}
