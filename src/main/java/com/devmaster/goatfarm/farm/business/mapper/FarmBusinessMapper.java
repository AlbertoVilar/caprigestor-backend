package com.devmaster.goatfarm.farm.business.mapper;

import com.devmaster.goatfarm.farm.application.model.FarmRecord;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmFullResponseVO;
import com.devmaster.goatfarm.farm.business.bo.GoatFarmResponseVO;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

@Component
public class FarmBusinessMapper {
    public GoatFarmFullResponseVO toFullResponseVO(FarmRecord farm) {
        GoatFarmFullResponseVO response = new GoatFarmFullResponseVO();
        response.setId(farm.id()); response.setName(farm.name()); response.setTod(farm.tod()); response.setLogoUrl(farm.logoUrl()); response.setVersion(farm.version());
        response.setCreatedAt(toLocal(farm.createdAt())); response.setUpdatedAt(toLocal(farm.updatedAt()));
        if (farm.owner() != null) { response.setUserId(farm.owner().id()); response.setUserName(farm.owner().name()); response.setUserEmail(farm.owner().email()); response.setUserCpf(farm.owner().cpf()); response.setUserRoles(farm.owner().roles()); }
        if (farm.address() != null) { response.setAddressId(farm.address().getId()); response.setStreet(farm.address().getStreet()); response.setDistrict(farm.address().getNeighborhood()); response.setCity(farm.address().getCity()); response.setState(farm.address().getState()); response.setZipCode(farm.address().getZipCode()); response.setCountry(farm.address().getCountry()); }
        response.setPhones(farm.phones()); return response;
    }
    public GoatFarmResponseVO toResponseVO(FarmRecord farm) {
        return new GoatFarmResponseVO(farm.id(), farm.name(), farm.tod(), farm.logoUrl(), toLocal(farm.createdAt()), toLocal(farm.updatedAt()));
    }
    private java.time.LocalDateTime toLocal(java.time.Instant value) { return value == null ? null : java.time.LocalDateTime.ofInstant(value, ZoneId.systemDefault()); }
}
