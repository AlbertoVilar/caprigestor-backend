package com.devmaster.goatfarm.commercial.application.ports.in;

import com.devmaster.goatfarm.commercial.business.bo.OwnershipSaleRequestVO;
import com.devmaster.goatfarm.commercial.business.bo.OwnershipSaleResponseVO;
import com.devmaster.goatfarm.commercial.business.bo.SalePaymentRequestVO;

import java.util.List;

/** Boundary for sales that transfer ownership only through the canonical ledger. */
public interface OwnershipSaleUseCase {
    OwnershipSaleResponseVO requestOwnershipSale(Long sourceFarmId, OwnershipSaleRequestVO request);
    OwnershipSaleResponseVO acceptOwnershipSale(Long sourceFarmId, Long saleId);
    OwnershipSaleResponseVO registerOwnershipSalePayment(Long sourceFarmId, Long saleId, SalePaymentRequestVO payment);
    OwnershipSaleResponseVO rejectOwnershipSale(Long sourceFarmId, Long saleId);
    OwnershipSaleResponseVO cancelOwnershipSale(Long sourceFarmId, Long saleId);
    List<OwnershipSaleResponseVO> listIncomingOwnershipSales(Long targetFarmId);
    List<OwnershipSaleResponseVO> listOutgoingOwnershipSales(Long sourceFarmId);
}
