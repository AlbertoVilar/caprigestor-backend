package com.devmaster.goatfarm.application.ports.out;

import com.devmaster.goatfarm.goat.model.entity.Goat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Porta de saída para persistência de cabras
 * Define as operações de persistência necessárias para cabras
 */
public interface GoatPersistencePort {

    /**
     * Salva uma cabra
     * @param goat Cabra a ser salva
     * @return Cabra salva
     */
    Goat save(Goat goat);

    /**
     * Busca uma cabra pelo registrationNumber (ID primário)
     * @param registrationNumber Número de registro da cabra
     * @return Optional contendo a cabra se encontrada
     */
    Optional<Goat> findById(String registrationNumber);

    /**
     * Alias explícito para busca por número de registro
     * @param registrationNumber Número de registro da cabraA
     * @return Optional contendo a cabra se encontrada
     */
    Optional<Goat> findByRegistrationNumber(String registrationNumber);

    /**
     * Busca todas as cabras de um capril
     * @param goatFarmId ID do capril
     * @return Lista de cabras do capril
     */
    List<Goat> findByGoatFarmId(Long goatFarmId);

    /**
     * Busca cabras de um capril com paginação
     * @param goatFarmId ID do capril
     * @param pageable Configuração de paginação
     * @return Página de cabras
     */
    Page<Goat> findAllByFarmId(Long goatFarmId, Pageable pageable);

    /**
     * Busca cabras por nome e capril com paginação
     * @param goatFarmId ID do capril
     * @param name Nome para filtro
     * @param pageable Configuração de paginação
     * @return Página de cabras filtradas
     */
    Page<Goat> findByNameAndFarmId(Long goatFarmId, String name, Pageable pageable);

    /**
     * Busca cabra por registrationNumber e farmId
     * @param id Número de registro
     * @param farmId ID do capril
     * @return Optional contendo a cabra se encontrada
     */
    Optional<Goat> findByIdAndFarmId(String id, Long farmId);

    Optional<Goat> findByIdAndFarmIdWithFamilyGraph(String id, Long farmId);

    /**
     * Remove uma cabra pelo registrationNumber
     * @param registrationNumber Número de registro da cabra
     */
    void deleteById(String registrationNumber);

    /**
     * Verifica se uma cabra existe por número de registro
     * @param registrationNumber Número de registro da cabra
     * @return true se a cabra existe
     */
    boolean existsByRegistrationNumber(String registrationNumber);

    /**
     * Remove cabras de outros usuários (operação administrativa)
     * @param adminId ID do administrador
     */
    void deleteGoatsFromOtherUsers(Long adminId);
}