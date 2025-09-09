package com.devmaster.goatfarm.genealogy.converter;

import com.devmaster.goatfarm.genealogy.business.bo.GenealogyResponseVO;
import com.devmaster.goatfarm.genealogy.model.entity.Genealogy;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@Service
public class GenealogyConverter {

    private final Map<String, BiConsumer<GenealogyResponseVO, Goat>> goatToResponseMappers = new HashMap<>();

    public GenealogyConverter() {
        // Direct mapping of the main goat's attributes
        goatToResponseMappers.put("goat", (response, goat) -> {
            response.setGoatName(goat.getName());
            response.setGoatRegistration(goat.getRegistrationNumber());
            response.setBreeder(goat.getFarm() != null ? goat.getFarm().getName() : null);
            response.setOwner(goat.getUser() != null ? goat.getUser().getName() : null);
            response.setBreed(goat.getBreed() != null ? goat.getBreed().name() : null);
            response.setColor(goat.getColor());
            response.setStatus(goat.getStatus() != null ? goat.getStatus().name() : null);
            response.setGender(goat.getGender() != null ? goat.getGender().name() : null);
            response.setCategory(goat.getCategory() != null ? goat.getCategory().name() : null);
            response.setTod(goat.getTod());
            response.setToe(goat.getToe());
            response.setBirthDate(goat.getBirthDate() != null ? goat.getBirthDate().toString() : null);
        });

        // Mapping for father
        goatToResponseMappers.put("father", (response, father) -> {
            response.setFatherName(father.getName());
            response.setFatherRegistration(father.getRegistrationNumber());
        });

        // Mapping for mother
        goatToResponseMappers.put("mother", (response, mother) -> {
            response.setMotherName(mother.getName());
            response.setMotherRegistration(mother.getRegistrationNumber());
        });

        // Mapping for paternal grandfathers
        goatToResponseMappers.put("paternalGrandfather", (response, grandfather) -> {
            response.setPaternalGrandfatherName(grandfather.getName());
            response.setPaternalGrandfatherRegistration(grandfather.getRegistrationNumber());
        });

        goatToResponseMappers.put("paternalGrandmother", (response, grandmother) -> {
            response.setPaternalGrandmotherName(grandmother.getName());
            response.setPaternalGrandmotherRegistration(grandmother.getRegistrationNumber());
        });

        // Mapping for maternal grandfathers
        goatToResponseMappers.put("maternalGrandfather", (response, grandfather) -> {
            response.setMaternalGrandfatherName(grandfather.getName());
            response.setMaternalGrandfatherRegistration(grandfather.getRegistrationNumber());
        });

        goatToResponseMappers.put("maternalGrandmother", (response, grandmother) -> {
            response.setMaternalGrandmotherName(grandmother.getName());
            response.setMaternalGrandmotherRegistration(grandmother.getRegistrationNumber());
        });

        // Mapping for paternal great-grandfathers (grandfather's side)
        goatToResponseMappers.put("paternalGreatGrandfather1", (response, greatGrandfather) -> {
            response.setPaternalGreatGrandfather1Name(greatGrandfather.getName());
            response.setPaternalGreatGrandfather1Registration(greatGrandfather.getRegistrationNumber());
        });

        goatToResponseMappers.put("paternalGreatGrandmother1", (response, greatGrandmother) -> {
            response.setPaternalGreatGrandmother1Name(greatGrandmother.getName());
            response.setPaternalGreatGrandmother1Registration(greatGrandmother.getRegistrationNumber());
        });

        // Mapping for paternal great-grandfathers (grandmother's side)
        goatToResponseMappers.put("paternalGreatGrandfather2", (response, greatGrandfather) -> {
            response.setPaternalGreatGrandfather2Name(greatGrandfather.getName());
            response.setPaternalGreatGrandfather2Registration(greatGrandfather.getRegistrationNumber());
        });

        goatToResponseMappers.put("paternalGreatGrandmother2", (response, greatGrandmother) -> {
            response.setPaternalGreatGrandmother2Name(greatGrandmother.getName());
            response.setPaternalGreatGrandmother2Registration(greatGrandmother.getRegistrationNumber());
        });

        // Mapping for maternal great-grandfathers (grandfather's side)
        goatToResponseMappers.put("maternalGreatGrandfather1", (response, greatGrandfather) -> {
            response.setMaternalGreatGrandfather1Name(greatGrandfather.getName());
            response.setMaternalGreatGrandfather1Registration(greatGrandfather.getRegistrationNumber());
        });

        goatToResponseMappers.put("maternalGreatGrandmother1", (response, greatGrandmother) -> {
            response.setMaternalGreatGrandmother1Name(greatGrandmother.getName());
            response.setMaternalGreatGrandmother1Registration(greatGrandmother.getRegistrationNumber());
        });

        // Mapping for maternal great-grandfathers (grandmother's side)
        goatToResponseMappers.put("maternalGreatGrandfather2", (response, greatGrandfather) -> {
            response.setMaternalGreatGrandfather2Name(greatGrandfather.getName());
            response.setMaternalGreatGrandfather2Registration(greatGrandfather.getRegistrationNumber());
        });

        goatToResponseMappers.put("maternalGreatGrandmother2", (response, greatGrandmother) -> {
            response.setMaternalGreatGrandmother2Name(greatGrandmother.getName());
            response.setMaternalGreatGrandmother2Registration(greatGrandmother.getRegistrationNumber());
        });
    }

    public GenealogyResponseVO buildGenealogyFromGoat(Goat goat) {
        GenealogyResponseVO genealogy = new GenealogyResponseVO();

        // Maps the data of the main goat
        goatToResponseMappers.get("goat").accept(genealogy, goat);

        // Maps father and its ancestors
        if (goat.getFather() != null) {
            goatToResponseMappers.get("father").accept(genealogy, goat.getFather());
            mapAncestors(goat.getFather(), genealogy, "paternal");
        }

        // Maps mother and its ancestors
        if (goat.getMother() != null) {
            goatToResponseMappers.get("mother").accept(genealogy, goat.getMother());
            mapAncestors(goat.getMother(), genealogy, "maternal");
        }

        return genealogy;
    }

    private void mapAncestors(Goat parent, GenealogyResponseVO genealogy, String side) {
        // Grandparents
        Goat grandfather = parent.getFather();
        Goat grandmother = parent.getMother();

        if (grandfather != null) {
            goatToResponseMappers.get(side + "Grandfather").accept(genealogy, grandfather);
            mapGreatGrandparents(grandfather, genealogy, side, 1);
        }

        if (grandmother != null) {
            goatToResponseMappers.get(side + "Grandmother").accept(genealogy, grandmother);
            mapGreatGrandparents(grandmother, genealogy, side, 2);
        }
    }

    private void mapGreatGrandparents(Goat grandparent, GenealogyResponseVO genealogy, String side, int index) {
        Goat greatGrandfather = grandparent.getFather();
        Goat greatGrandmother = grandparent.getMother();

        String greatGrandfatherKey = side + "GreatGrandfather" + index;
        String greatGrandmotherKey = side + "GreatGrandmother" + index;

        if (greatGrandfather != null && goatToResponseMappers.containsKey(greatGrandfatherKey)) {
            goatToResponseMappers.get(greatGrandfatherKey).accept(genealogy, greatGrandfather);
        }

        if (greatGrandmother != null && goatToResponseMappers.containsKey(greatGrandmotherKey)) {
            goatToResponseMappers.get(greatGrandmotherKey).accept(genealogy, greatGrandmother);
        }
    }

    // Converts to entity, to save the genealogy in the database, if desired
    private Genealogy convertToGenealogyEntity(Goat goat) {
        Map<String, BiConsumer<Genealogy, Goat>> goatToEntityMappers = new HashMap<>();

        // Direct mapping of the main goat's attributes to the Genealogy entity
        goatToEntityMappers.put("goat", (entity, goatData) -> {
            entity.setGoatName(goatData.getName());
            entity.setGoatRegistration(goatData.getRegistrationNumber());
            entity.setGoatCreator(goatData.getFarm() != null ? goatData.getFarm().getName() : null);
            entity.setGoatOwner(goatData.getUser() != null ? goatData.getUser().getName() : null);
            entity.setGoatBreed(goatData.getBreed() != null ? goatData.getBreed().name() : null);
            entity.setGoatCoatColor(goatData.getColor());
            entity.setGoatStatus(goatData.getStatus() != null ? goatData.getStatus().name() : null);
            entity.setGoatSex(goatData.getGender() != null ? goatData.getGender().name() : null);
            entity.setGoatCategory(goatData.getCategory() != null ? goatData.getCategory().name() : null);
            entity.setGoatTOD(goatData.getTod());
            entity.setGoatTOE(goatData.getToe());
            entity.setGoatBirthDate(goatData.getBirthDate() != null ? goatData.getBirthDate().toString() : null);
        });

        // Mapping for father
        goatToEntityMappers.put("father", (entity, father) -> {
            entity.setFatherName(father.getName());
            entity.setFatherRegistration(father.getRegistrationNumber());
        });

        // Mapping for mother
        goatToEntityMappers.put("mother", (entity, mother) -> {
            entity.setMotherName(mother.getName());
            entity.setMotherRegistration(mother.getRegistrationNumber());
        });

        // Mapping for paternal grandfathers
        goatToEntityMappers.put("paternalGrandfather", (entity, grandfather) -> {
            entity.setPaternalGrandfatherName(grandfather.getName());
            entity.setPaternalGrandfatherRegistration(grandfather.getRegistrationNumber());
        });

        goatToEntityMappers.put("paternalGrandmother", (entity, grandmother) -> {
            entity.setPaternalGrandmotherName(grandmother.getName());
            entity.setPaternalGrandmotherRegistration(grandmother.getRegistrationNumber());
        });

        // Mapping for maternal grandfathers
        goatToEntityMappers.put("maternalGrandfather", (entity, grandfather) -> {
            entity.setMaternalGrandfatherName(grandfather.getName());
            entity.setMaternalGrandfatherRegistration(grandfather.getRegistrationNumber());
        });

        goatToEntityMappers.put("maternalGrandmother", (entity, grandmother) -> {
            entity.setMaternalGrandmotherName(grandmother.getName());
            entity.setMaternalGrandmotherRegistration(grandmother.getRegistrationNumber());
        });

        // Mapping for paternal great-grandfathers (grandfather's side)
        goatToEntityMappers.put("paternalGreatGrandfather1", (entity, greatGrandfather) -> {
            entity.setPaternalGreatGrandfather1Name(greatGrandfather.getName());
            entity.setPaternalGreatGrandfather1Registration(greatGrandfather.getRegistrationNumber());
        });

        goatToEntityMappers.put("paternalGreatGrandmother1", (entity, greatGrandmother) -> {
            entity.setPaternalGreatGrandmother1Name(greatGrandmother.getName());
            entity.setPaternalGreatGrandmother1Registration(greatGrandmother.getRegistrationNumber());
        });

        // Mapping for paternal great-grandfathers (grandmother's side)
        goatToEntityMappers.put("paternalGreatGrandfather2", (entity, greatGrandfather) -> {
            entity.setPaternalGreatGrandfather2Name(greatGrandfather.getName());
            entity.setPaternalGreatGrandfather2Registration(greatGrandfather.getRegistrationNumber());
        });

        goatToEntityMappers.put("paternalGreatGrandmother2", (entity, greatGrandmother) -> {
            entity.setPaternalGreatGrandmother2Name(greatGrandmother.getName());
            entity.setPaternalGreatGrandmother2Registration(greatGrandmother.getRegistrationNumber());
        });

        // Mapping for maternal great-grandfathers (grandfather's side)
        goatToEntityMappers.put("maternalGreatGrandfather1", (entity, greatGrandfather) -> {
            entity.setMaternalGreatGrandfather1Name(greatGrandfather.getName());
            entity.setMaternalGreatGrandfather1Registration(greatGrandfather.getRegistrationNumber());
        });

        goatToEntityMappers.put("maternalGreatGrandmother1", (entity, greatGrandmother) -> {
            entity.setMaternalGreatGrandmother1Name(greatGrandmother.getName());
            entity.setMaternalGreatGrandmother1Registration(greatGrandmother.getRegistrationNumber());
        });

        // Mapping for maternal great-grandfathers (grandmother's side)
        goatToEntityMappers.put("maternalGreatGrandfather2", (entity, greatGrandfather) -> {
            entity.setMaternalGreatGrandfather2Name(greatGrandfather.getName());
            entity.setMaternalGreatGrandfather2Registration(greatGrandfather.getRegistrationNumber());
        });

        goatToEntityMappers.put("maternalGreatGrandmother2", (entity, greatGrandmother) -> {
            entity.setMaternalGreatGrandmother2Name(greatGrandmother.getName());
            entity.setMaternalGreatGrandmother2Registration(greatGrandmother.getRegistrationNumber());
        });

        Genealogy genealogyEntity = Genealogy.builder().build();

        // Maps the data of the main goat to the entity
        goatToEntityMappers.get("goat").accept(genealogyEntity, goat);

        // Maps father and its ancestors to the entity
        if (goat.getFather() != null) {
            goatToEntityMappers.get("father").accept(genealogyEntity, goat.getFather());
            mapAncestorsToEntity(goat.getFather(), genealogyEntity, "paternal", goatToEntityMappers);
        }

        // Maps mother and its ancestors to the entity
        if (goat.getMother() != null) {
            goatToEntityMappers.get("mother").accept(genealogyEntity, goat.getMother());
            mapAncestorsToEntity(goat.getMother(), genealogyEntity, "maternal", goatToEntityMappers);
        }

        return genealogyEntity;
    }

    private void mapAncestorsToEntity(Goat parent, Genealogy genealogyEntity, String side, Map<String, BiConsumer<Genealogy, Goat>> mappers) {
        // Grandparents
        Goat grandfather = parent.getFather();
        Goat grandmother = parent.getMother();

        if (grandfather != null) {
            mappers.get(side + "Grandfather").accept(genealogyEntity, grandfather);
            mapGreatGrandparentsToEntity(grandfather, genealogyEntity, side, 1, mappers);
        }

        if (grandmother != null) {
            mappers.get(side + "Grandmother").accept(genealogyEntity, grandmother);
            mapGreatGrandparentsToEntity(grandmother, genealogyEntity, side, 2, mappers);
        }
    }

    private static void mapGreatGrandparentsToEntity(Goat grandparent, Genealogy genealogyEntity, String side, int index, Map<String, BiConsumer<Genealogy, Goat>> mappers) {
        Goat greatGrandfather = grandparent.getFather();
        Goat greatGrandmother = grandparent.getMother();

        String greatGrandfatherKey = side + "GreatGrandfather" + index;
        String greatGrandmotherKey = side + "GreatGrandmother" + index;

        if (greatGrandfather != null && mappers.containsKey(greatGrandfatherKey)) {
            mappers.get(greatGrandfatherKey).accept(genealogyEntity, greatGrandfather);
        }

        if (greatGrandmother != null && mappers.containsKey(greatGrandmotherKey)) {
            mappers.get(greatGrandmotherKey).accept(genealogyEntity, greatGrandmother);
        }
    }
}