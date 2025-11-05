package com.devmaster.goatfarm.application.ports.out;

import com.devmaster.goatfarm.goat.model.entity.Goat;

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
     * @return Cabra salva com ID gerado
     */
    Goat save(Goat goat);

    /**
     * Busca uma cabra por ID
     * @param id ID da cabra
     * @return Optional contendo a cabra se encontrada
     */
    Optional<Goat> findById(Long id);

    /**
     * Busca uma cabra por número de registro
     * @param registrationNumber Número de registro da cabra
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
     * Remove uma cabra por ID
     * @param id ID da cabra
     */
    void deleteById(Long id);

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