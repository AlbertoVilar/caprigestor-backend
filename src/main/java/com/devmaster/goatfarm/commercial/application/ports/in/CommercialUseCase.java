package com.devmaster.goatfarm.commercial.application.ports.in;

import com.devmaster.goatfarm.commercial.business.bo.AnimalSaleRequestVO;
import com.devmaster.goatfarm.commercial.business.bo.AnimalSaleResponseVO;
import com.devmaster.goatfarm.commercial.business.bo.CommercialSummaryVO;
import com.devmaster.goatfarm.commercial.business.bo.CustomerRequestVO;
import com.devmaster.goatfarm.commercial.business.bo.CustomerResponseVO;
import com.devmaster.goatfarm.commercial.business.bo.MilkSaleRequestVO;
import com.devmaster.goatfarm.commercial.business.bo.MilkSaleResponseVO;
import com.devmaster.goatfarm.commercial.business.bo.ReceivableResponseVO;
import com.devmaster.goatfarm.commercial.business.bo.SalePaymentRequestVO;

import java.util.List;

public interface CommercialUseCase {

    CustomerResponseVO createCustomer(Long farmId, CustomerRequestVO requestVO);

    List<CustomerResponseVO> listCustomers(Long farmId);

    AnimalSaleResponseVO createAnimalSale(Long farmId, AnimalSaleRequestVO requestVO);

    List<AnimalSaleResponseVO> listAnimalSales(Long farmId);

    AnimalSaleResponseVO registerAnimalSalePayment(Long farmId, Long saleId, SalePaymentRequestVO requestVO);

    MilkSaleResponseVO createMilkSale(Long farmId, MilkSaleRequestVO requestVO);

    List<MilkSaleResponseVO> listMilkSales(Long farmId);

    MilkSaleResponseVO registerMilkSalePayment(Long farmId, Long saleId, SalePaymentRequestVO requestVO);

    List<ReceivableResponseVO> listReceivables(Long farmId);

    CommercialSummaryVO getSummary(Long farmId);
}
