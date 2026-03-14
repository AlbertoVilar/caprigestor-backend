package com.devmaster.goatfarm.genealogy.application.ports.out;

import com.devmaster.goatfarm.genealogy.business.bo.GenealogyAbccSnapshotVO;

import java.util.Optional;

public interface GenealogyAbccQueryPort {

    Optional<GenealogyAbccSnapshotVO> findGenealogyByRegistrationNumber(String registrationNumber);
}

