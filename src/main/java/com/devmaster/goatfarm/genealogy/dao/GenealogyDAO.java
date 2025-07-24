package com.devmaster.goatfarm.genealogy.dao;

import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.converter.GenealogyEntityConverter;
import com.devmaster.goatfarm.genealogy.model.entity.Genealogy;
import com.devmaster.goatfarm.genealogy.model.repository.GenealogyRepository;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GenealogyDAO {

    @Autowired
    private GenealogyRepository genealogyRepository;

    @Autowired
    private GoatRepository goatRepository;

    @Autowired
    private com.devmaster.goatfarm.genealogy.converter.GenealogyConverter buildGenealogyMapper;

    /**
     * Cria e salva a genealogia completa de um animal com base no seu número de registro.
     *
     * <p>Este método segue os seguintes passos:
     * <ol>
     *   <li>Verifica se já existe uma genealogia para o animal. Se existir, lança exceção.</li>
     *   <li>Busca o animal (Goat) pelo número de registro. Se não encontrado, lança exceção.</li>
     *   <li>Utiliza o {@code GenealogyConverter} para construir a árvore genealógica (VO).</li>
     *   <li>Converte o VO para entidade Genealogy e salva no banco.</li>
     *   <li>Retorna a genealogia construída como um {@code GenealogyResponseVO}.</li>
     * </ol>
     *
     * <p>Este método é transacional. Se qualquer exceção ocorrer, a operação será revertida.
     *
     * @param goatRegistrationNumber o número de registro do animal cuja genealogia será criada
     * @return {@code GenealogyResponseVO} representando a genealogia do animal
     * @throws ResourceNotFoundException se o animal com o número de registro informado não for encontrado
     * @throws DatabaseException se a genealogia já existir ou ocorrer qualquer erro ao persistir os dados
     */
    @Transactional
    public GenealogyResponseVO createGenealogy(String goatRegistrationNumber) {
        if (genealogyRepository.existsByGoatRegistration(goatRegistrationNumber)) {
            throw new DatabaseException("A genealogia do animal " + goatRegistrationNumber + " já existe.");
        }

        final Goat goat = goatRepository.findByRegistrationNumber(goatRegistrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Animal não encontrado com o número de registro: " + goatRegistrationNumber));

        try {
            final GenealogyResponseVO response = buildGenealogyMapper.buildGenealogyFromGoat(goat);
            final Genealogy entity = GenealogyEntityConverter.toEntity(response);
            genealogyRepository.save(entity);
            return response;

        } catch (Exception e) {
            throw new DatabaseException("Erro ao criar a genealogia do animal " + goatRegistrationNumber + ": " + e.getMessage());
        }
    }

    /**
     * Busca a genealogia previamente salva de um animal com base no número de registro.
     *
     * @param goatRegistrationNumber o número de registro do animal
     * @return {@code GenealogyResponseVO} representando a genealogia do animal
     * @throws ResourceNotFoundException se nenhuma genealogia for encontrada para o animal
     * @throws DatabaseException se ocorrer qualquer erro durante a consulta
     */
    @Transactional(readOnly = true)
    public GenealogyResponseVO findGenealogy(String goatRegistrationNumber) {
        try {
            final Optional<Genealogy> genealogyOptional = genealogyRepository.findByGoatRegistration(goatRegistrationNumber);

            return genealogyOptional
                    .map(GenealogyEntityConverter::toResponseVO)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Genealogia não encontrada com o número de registro: " + goatRegistrationNumber));

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException("Erro ao buscar a genealogia do animal " + goatRegistrationNumber + ": " + e.getMessage());
        }
    }
}
