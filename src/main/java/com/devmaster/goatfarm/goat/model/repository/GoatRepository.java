// Em GoatRepository.java
package com.devmaster.goatfarm.goat.model.repository;

import com.devmaster.goatfarm.goat.model.entity.Goat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GoatRepository extends JpaRepository<Goat, String> {

    /**
     * Busca uma cabra pelo número de registro, carregando também pai, mãe, fazenda e proprietário.
     * Retorna um Optional<Goat> para um único resultado.
     */
    @Query("""
        SELECT g FROM Goat g
        LEFT JOIN FETCH g.father
        LEFT JOIN FETCH g.farm
        LEFT JOIN FETCH g.user
        WHERE g.registrationNumber = :registrationNumber
    """)
    Optional<Goat> findByRegistrationNumber(@Param("registrationNumber") String registrationNumber);

    /**
     * Lista paginada de todas as cabras sem filtros.
     */
    Page<Goat> findAll(Pageable pageable);

    /**
     * Busca paginada de cabras por nome (sem considerar a fazenda).
     */
    @Query("""
        SELECT g FROM Goat g
        WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    Page<Goat> searchGoatByName(@Param("name") String name, Pageable pageable);

    /**
     * SOLUÇÃO DEFINITIVA: Busca paginada de cabras por ID da fazenda, com filtro opcional por número de registro.
     * Utiliza uma query nativa com CAST explícito para evitar o erro de tipo 'bytea'.
     */
    @Query(value = """
        SELECT g1_0.num_registro,
               g1_0.nascimento,
               g1_0.raca,
               g1_0.categoria,
               g1_0.cor,
               g1_0.capril_id,
               g1_0.pai_id,
               g1_0.sexo,
               g1_0.mae_id,
               g1_0.nome,
               g1_0.user_id,
               g1_0.status,
               g1_0.tod,
               g1_0.toe
        FROM cabras g1_0
        WHERE g1_0.capril_id = :farmId
        AND (:registrationNumber IS NULL OR g1_0.num_registro LIKE ('%' || CAST(:registrationNumber AS VARCHAR) || '%'))
    """,
            countQuery = """
        SELECT count(g1_0.num_registro)
        FROM cabras g1_0
        WHERE g1_0.capril_id = :farmId
        AND (:registrationNumber IS NULL OR g1_0.num_registro LIKE ('%' || CAST(:registrationNumber AS VARCHAR) || '%'))
    """,
            nativeQuery = true)
    Page<Goat> findByFarmIdAndOptionalRegistrationNumber(
            @Param("farmId") Long farmId,
            @Param("registrationNumber") String registrationNumber,
            Pageable pageable);

    /**
     * Busca paginada de cabras por nome dentro de uma fazenda específica.
     */
    @Query("""
        SELECT g FROM Goat g
        WHERE g.farm.id = :farmId
        AND LOWER(g.name) LIKE LOWER(CONCAT('%', :name, '%'))
    """)
    Page<Goat> findByNameAndFarmId(
            @Param("farmId") Long farmId,
            @Param("name") String name,
            Pageable pageable
    );
}