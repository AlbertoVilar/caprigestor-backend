package com.devmaster.goatfarm.genealogy.dao;

import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.genealogy.api.dto.GenealogyRequestDTO;
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

    /**
     * Cria e salva uma genealogia com dados completos fornecidos via DTO.
     *
     * @param requestDTO o DTO contendo todos os dados da genealogia
     * @return {@code GenealogyResponseVO} representando a genealogia criada
     * @throws DatabaseException se a genealogia já existir ou ocorrer erro ao persistir
     */
    @Transactional
    public GenealogyResponseVO createGenealogyWithData(GenealogyRequestDTO requestDTO) {
        if (genealogyRepository.existsByGoatRegistration(requestDTO.getGoatRegistration())) {
            throw new DatabaseException("A genealogia do animal " + requestDTO.getGoatRegistration() + " já existe.");
        }

        try {
            // Converter DTO diretamente para entidade
            final Genealogy entity = convertRequestDTOToEntity(requestDTO);
            final Genealogy savedEntity = genealogyRepository.save(entity);
            
            // Convert saved entity to ResponseVO
            return GenealogyEntityConverter.toResponseVO(savedEntity);

        } catch (Exception e) {
            throw new DatabaseException("Erro ao criar a genealogia do animal " + requestDTO.getGoatRegistration() + ": " + e.getMessage());
        }
    }

    /**
     * Converte GenealogyRequestDTO para entidade Genealogy.
     */
    private Genealogy convertRequestDTOToEntity(GenealogyRequestDTO dto) {
        return Genealogy.builder()
                .goatName(dto.getGoatName())
                .goatRegistration(dto.getGoatRegistration())
                .goatCreator(dto.getBreeder())
                .goatOwner(dto.getFarmOwner())
                .goatBreed(dto.getBreed())
                .goatCoatColor(dto.getColor())
                .goatStatus(dto.getStatus())
                .goatSex(dto.getGender())
                .goatCategory(dto.getCategory())
                .goatTOD(dto.getTod())
                .goatTOE(dto.getToe())
                .goatBirthDate(dto.getBirthDate())
                .fatherName(dto.getFatherName())
                .fatherRegistration(dto.getFatherRegistration())
                .motherName(dto.getMotherName())
                .motherRegistration(dto.getMotherRegistration())
                .paternalGrandfatherName(dto.getPaternalGrandfatherName())
                .paternalGrandfatherRegistration(dto.getPaternalGrandfatherRegistration())
                .paternalGrandmotherName(dto.getPaternalGrandmotherName())
                .paternalGrandmotherRegistration(dto.getPaternalGrandmotherRegistration())
                .maternalGrandfatherName(dto.getMaternalGrandfatherName())
                .maternalGrandfatherRegistration(dto.getMaternalGrandfatherRegistration())
                .maternalGrandmotherName(dto.getMaternalGrandmotherName())
                .maternalGrandmotherRegistration(dto.getMaternalGrandmotherRegistration())
                .paternalGreatGrandfather1Name(dto.getPaternalGreatGrandfather1Name())
                .paternalGreatGrandfather1Registration(dto.getPaternalGreatGrandfather1Registration())
                .paternalGreatGrandmother1Name(dto.getPaternalGreatGrandmother1Name())
                .paternalGreatGrandmother1Registration(dto.getPaternalGreatGrandmother1Registration())
                .paternalGreatGrandfather2Name(dto.getPaternalGreatGrandfather2Name())
                .paternalGreatGrandfather2Registration(dto.getPaternalGreatGrandfather2Registration())
                .paternalGreatGrandmother2Name(dto.getPaternalGreatGrandmother2Name())
                .paternalGreatGrandmother2Registration(dto.getPaternalGreatGrandmother2Registration())
                .maternalGreatGrandfather1Name(dto.getMaternalGreatGrandfather1Name())
                .maternalGreatGrandfather1Registration(dto.getMaternalGreatGrandfather1Registration())
                .maternalGreatGrandmother1Name(dto.getMaternalGreatGrandmother1Name())
                .maternalGreatGrandmother1Registration(dto.getMaternalGreatGrandmother1Registration())
                .maternalGreatGrandfather2Name(dto.getMaternalGreatGrandfather2Name())
                .maternalGreatGrandfather2Registration(dto.getMaternalGreatGrandfather2Registration())
                .maternalGreatGrandmother2Name(dto.getMaternalGreatGrandmother2Name())
                .maternalGreatGrandmother2Registration(dto.getMaternalGreatGrandmother2Registration())
                .build();
    }
}
