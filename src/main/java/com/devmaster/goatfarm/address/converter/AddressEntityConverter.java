package com.devmaster.goatfarm.address.converter;

import com.devmaster.goatfarm.address.business.bo.AddressRequestVO;
import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.address.model.entity.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressEntityConverter {

    public static Address toEntity(AddressRequestVO requestVO) {
        
        // Debug logs
        System.out.println("DEBUG - AddressEntityConverter.toEntity:");
        System.out.println("  Street: " + requestVO.getStreet());
        System.out.println("  Neighborhood: " + requestVO.getNeighborhood());
        System.out.println("  City: " + requestVO.getCity());
        System.out.println("  State: " + requestVO.getState());
        System.out.println("  PostalCode: " + requestVO.getPostalCode());
        System.out.println("  Country: " + requestVO.getCountry());

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
