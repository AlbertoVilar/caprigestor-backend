package com.devmaster.goatfarm.farm.application.model;

import com.devmaster.goatfarm.address.business.bo.AddressResponseVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;

import java.time.Instant;
import java.util.List;

/** Technology-neutral Farm projection used by application services and other modules. */
public record FarmRecord(Long id, String name, String tod, String logoUrl, OwnerReference owner,
                         AddressResponseVO address, List<PhoneResponseVO> phones,
                         Instant createdAt, Instant updatedAt, Integer version) {
    public FarmRecord { phones = phones == null ? List.of() : List.copyOf(phones); }
    public Long ownerUserId() { return owner == null ? null : owner.id(); }
    public Long addressId() { return address == null ? null : address.getId(); }
}
