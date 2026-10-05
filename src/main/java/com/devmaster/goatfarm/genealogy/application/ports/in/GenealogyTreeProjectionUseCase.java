package com.devmaster.goatfarm.genealogy.application.ports.in;

import com.devmaster.goatfarm.genealogy.application.model.GenealogyTreeSnapshot;
import com.devmaster.goatfarm.goat.application.model.GoatGenealogySnapshot;

public interface GenealogyTreeProjectionUseCase {
    GenealogyTreeSnapshot projectLocal(GoatGenealogySnapshot root);
    GenealogyTreeSnapshot complementWithAbcc(GoatGenealogySnapshot root);
}
