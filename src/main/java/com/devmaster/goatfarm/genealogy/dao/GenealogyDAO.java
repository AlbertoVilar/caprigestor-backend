package com.devmaster.goatfarm.genealogy.dao;

import com.devmaster.goatfarm.config.exceptions.custom.DatabaseException;
import com.devmaster.goatfarm.config.exceptions.custom.ResourceNotFoundException;
import com.devmaster.goatfarm.genealogy.api.dto.GenealogyRequestDTO;
import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.mapper.GenealogyMapper;
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
    private GenealogyMapper genealogyMapper;

    /**
     * Constrói uma entidade Genealogy a partir dos dados do animal e seus ancestrais.
     *
     * @param goat o animal para o qual construir a genealogia
     * @return a entidade Genealogy preenchida
     */
    private Genealogy buildGenealogyFromGoat(Goat goat) {
        System.out.println("DEBUG: Iniciando buildGenealogyFromGoat para animal: " + goat.getRegistrationNumber());
        Genealogy genealogy = new Genealogy();
        
        try {
            // Dados do animal principal
            System.out.println("DEBUG: Preenchendo dados básicos do animal...");
            genealogy.setGoatName(goat.getName());
            genealogy.setGoatRegistration(goat.getRegistrationNumber());
        genealogy.setGoatBreed(goat.getBreed() != null ? goat.getBreed().toString() : null);
        genealogy.setGoatCoatColor(goat.getColor());
        genealogy.setGoatStatus(goat.getStatus() != null ? goat.getStatus().toString() : null);
        genealogy.setGoatSex(goat.getGender() != null ? goat.getGender().toString() : null);
        genealogy.setGoatCategory(goat.getCategory() != null ? goat.getCategory().toString() : null);
        genealogy.setGoatTOD(goat.getTod());
        genealogy.setGoatTOE(goat.getToe());
        genealogy.setGoatBirthDate(goat.getBirthDate() != null ? goat.getBirthDate().toString() : null);
        
        // Dados do criador e proprietário (se disponíveis)
        if (goat.getUser() != null) {
            genealogy.setGoatCreator(goat.getUser().getName());
        }
        if (goat.getFarm() != null && goat.getFarm().getUser() != null) {
            genealogy.setGoatOwner(goat.getFarm().getUser().getName());
        }
        
        // Dados dos pais
        if (goat.getFather() != null) {
            genealogy.setFatherName(goat.getFather().getName());
            genealogy.setFatherRegistration(goat.getFather().getRegistrationNumber());
            
            // Avós paternos
            if (goat.getFather().getFather() != null) {
                genealogy.setPaternalGrandfatherName(goat.getFather().getFather().getName());
                genealogy.setPaternalGrandfatherRegistration(goat.getFather().getFather().getRegistrationNumber());
                
                // Bisavós paternos (lado paterno)
                if (goat.getFather().getFather().getFather() != null) {
                    genealogy.setPaternalGreatGrandfather1Name(goat.getFather().getFather().getFather().getName());
                    genealogy.setPaternalGreatGrandfather1Registration(goat.getFather().getFather().getFather().getRegistrationNumber());
                }
                if (goat.getFather().getFather().getMother() != null) {
                    genealogy.setPaternalGreatGrandmother1Name(goat.getFather().getFather().getMother().getName());
                    genealogy.setPaternalGreatGrandmother1Registration(goat.getFather().getFather().getMother().getRegistrationNumber());
                }
            }
            
            if (goat.getFather().getMother() != null) {
                genealogy.setPaternalGrandmotherName(goat.getFather().getMother().getName());
                genealogy.setPaternalGrandmotherRegistration(goat.getFather().getMother().getRegistrationNumber());
                
                // Bisavós paternos (lado materno)
                if (goat.getFather().getMother().getFather() != null) {
                    genealogy.setPaternalGreatGrandfather2Name(goat.getFather().getMother().getFather().getName());
                    genealogy.setPaternalGreatGrandfather2Registration(goat.getFather().getMother().getFather().getRegistrationNumber());
                }
                if (goat.getFather().getMother().getMother() != null) {
                    genealogy.setPaternalGreatGrandmother2Name(goat.getFather().getMother().getMother().getName());
                    genealogy.setPaternalGreatGrandmother2Registration(goat.getFather().getMother().getMother().getRegistrationNumber());
                }
            }
        }
        
        if (goat.getMother() != null) {
            genealogy.setMotherName(goat.getMother().getName());
            genealogy.setMotherRegistration(goat.getMother().getRegistrationNumber());
            
            // Avós maternos
            if (goat.getMother().getFather() != null) {
                genealogy.setMaternalGrandfatherName(goat.getMother().getFather().getName());
                genealogy.setMaternalGrandfatherRegistration(goat.getMother().getFather().getRegistrationNumber());
                
                // Bisavós maternos (lado paterno)
                if (goat.getMother().getFather().getFather() != null) {
                    genealogy.setMaternalGreatGrandfather1Name(goat.getMother().getFather().getFather().getName());
                    genealogy.setMaternalGreatGrandfather1Registration(goat.getMother().getFather().getFather().getRegistrationNumber());
                }
                if (goat.getMother().getFather().getMother() != null) {
                    genealogy.setMaternalGreatGrandmother1Name(goat.getMother().getFather().getMother().getName());
                    genealogy.setMaternalGreatGrandmother1Registration(goat.getMother().getFather().getMother().getRegistrationNumber());
                }
            }
            
            if (goat.getMother().getMother() != null) {
                genealogy.setMaternalGrandmotherName(goat.getMother().getMother().getName());
                genealogy.setMaternalGrandmotherRegistration(goat.getMother().getMother().getRegistrationNumber());
                
                // Bisavós maternos (lado materno)
                if (goat.getMother().getMother().getFather() != null) {
                    genealogy.setMaternalGreatGrandfather2Name(goat.getMother().getMother().getFather().getName());
                    genealogy.setMaternalGreatGrandfather2Registration(goat.getMother().getMother().getFather().getRegistrationNumber());
                }
                if (goat.getMother().getMother().getMother() != null) {
                    genealogy.setMaternalGreatGrandmother2Name(goat.getMother().getMother().getMother().getName());
                    genealogy.setMaternalGreatGrandmother2Registration(goat.getMother().getMother().getMother().getRegistrationNumber());
                }
            }
        }
        
        return genealogy;
        } catch (Exception e) {
            System.out.println("DEBUG: Erro em buildGenealogyFromGoat: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Erro ao construir genealogia: " + e.getMessage(), e);
        }
    }

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
        System.out.println("DEBUG: Iniciando createGenealogy para registro: " + goatRegistrationNumber);
        
        if (genealogyRepository.existsByGoatRegistration(goatRegistrationNumber)) {
            System.out.println("DEBUG: Genealogia já existe para o registro: " + goatRegistrationNumber);
            throw new DatabaseException("A genealogia do animal " + goatRegistrationNumber + " já existe.");
        }

        System.out.println("DEBUG: Buscando animal no banco de dados...");
        final Goat goat = goatRepository.findByRegistrationNumber(goatRegistrationNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Animal não encontrado com o número de registro: " + goatRegistrationNumber));

        System.out.println("DEBUG: Animal encontrado: " + goat.getName());
        
        try {
            System.out.println("DEBUG: Construindo genealogia...");
            // Construir a genealogia com os dados do animal e seus ancestrais
            final Genealogy entity = buildGenealogyFromGoat(goat);
            System.out.println("DEBUG: Genealogia construída, salvando no banco...");
            genealogyRepository.save(entity);
            System.out.println("DEBUG: Genealogia salva com sucesso!");
            return genealogyMapper.toResponseVO(entity);

        } catch (Exception e) {
            System.out.println("DEBUG: Erro ao criar genealogia: " + e.getMessage());
            e.printStackTrace();
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
            System.out.println("DEBUG: Buscando genealogia para registro: '" + goatRegistrationNumber + "'");
            System.out.println("DEBUG: Tamanho do registro: " + goatRegistrationNumber.length());
            
            // Testando diferentes métodos de consulta
            final Optional<Genealogy> genealogyOptional = genealogyRepository.findByGoatRegistration(goatRegistrationNumber);
            System.out.println("DEBUG: Resultado findByGoatRegistration - presente: " + genealogyOptional.isPresent());
            
            final Optional<Genealogy> genealogyCustom = genealogyRepository.findByGoatRegistrationCustom(goatRegistrationNumber);
            System.out.println("DEBUG: Resultado findByGoatRegistrationCustom - presente: " + genealogyCustom.isPresent());
            
            final Optional<Genealogy> genealogyNative = genealogyRepository.findByGoatRegistrationNative(goatRegistrationNumber);
            System.out.println("DEBUG: Resultado findByGoatRegistrationNative - presente: " + genealogyNative.isPresent());
            
            if (genealogyOptional.isPresent()) {
                System.out.println("DEBUG: Registro encontrado: " + genealogyOptional.get().getGoatRegistration());
            }

            return genealogyOptional
                    .map(genealogyMapper::toResponseVO)
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
        System.out.println("DEBUG: Iniciando createGenealogyWithData para animal: " + requestDTO.getGoatRegistration());
        
        if (genealogyRepository.existsByGoatRegistration(requestDTO.getGoatRegistration())) {
            System.out.println("DEBUG: Genealogia já existe para o animal: " + requestDTO.getGoatRegistration());
            throw new DatabaseException("A genealogia do animal " + requestDTO.getGoatRegistration() + " já existe.");
        }

        try {
            System.out.println("DEBUG: Convertendo DTO para entidade...");
            // Converter DTO diretamente para entidade
            final Genealogy entity = genealogyMapper.toEntity(requestDTO);
            System.out.println("DEBUG: Entidade criada com sucesso. ID: " + entity.getId() + ", Nome: " + entity.getGoatName());
            
            System.out.println("DEBUG: Salvando entidade no banco...");
            final Genealogy savedEntity = genealogyRepository.save(entity);
            System.out.println("DEBUG: Entidade salva com sucesso. ID: " + savedEntity.getId());
            
            System.out.println("DEBUG: Convertendo entidade para ResponseVO...");
            // Convert saved entity to ResponseVO
            GenealogyResponseVO responseVO = genealogyMapper.toResponseVO(savedEntity);
            System.out.println("DEBUG: ResponseVO criado com sucesso");
            
            return responseVO;

        } catch (Exception e) {
            System.err.println("DEBUG: Erro capturado - Tipo: " + e.getClass().getSimpleName());
            System.err.println("DEBUG: Mensagem do erro: " + e.getMessage());
            e.printStackTrace();
            throw new DatabaseException("Erro ao criar a genealogia do animal " + requestDTO.getGoatRegistration() + ": " + e.getMessage());
        }
    }
}
