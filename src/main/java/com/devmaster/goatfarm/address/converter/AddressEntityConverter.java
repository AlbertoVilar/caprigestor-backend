package com.devmaster.goatfarm.address.converter;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.model.entity.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AddressEntityConverter {

    private static final Logger logger = LoggerFactory.getLogger(AddressEntityConverter.class);

    public static Address toEntity(AddressRequestVO requestVO) {
        
        // Debug logs
        logger.debug("DEBUG - AddressEntityConverter.toEntity:");
        logger.debug("  Street: {}", requestVO.getStreet());
        logger.debug("  Neighborhood: {}", requestVO.getNeighborhood());
        logger.debug("  City: {}", requestVO.getCity());
        logger.debug("  State: {}", requestVO.getState());
        logger.debug("  PostalCode: {}", requestVO.getPostalCode());
        logger.debug("  Country: {}", requestVO.getCountry());

        return new Address(
                null,
                requestVO.getStreet(),
                requestVO.getNeighborhood(),
                requestVO.getCity(),
                requestVO.getState(),
                requestVO.getPostalCode(),
                requestVO.getCountry()
        );
    }

    public static void toUpdateEntity(Address address, AddressRequestVO requestVO) {

        address.setStreet(requestVO.getStreet());
        address.setNeighborhood(requestVO.getNeighborhood());
        address.setCity(requestVO.getCity());
        address.setState(requestVO.getState());
        address.setPostalCode(requestVO.getPostalCode());
        address.setCountry(requestVO.getCountry());
    }

    public static AddressResponseVO toVO(Address address) {

        return new AddressResponseVO(

                address.getId(),
                address.getStreet(),
                address.getNeighborhood(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry()
        );
    }
}
