package com.devmaster.goatfarm.commercial.api.mapper;

import com.devmaster.goatfarm.commercial.api.dto.AnimalSaleRequestDTO;
import com.devmaster.goatfarm.commercial.api.dto.AnimalSaleResponseDTO;
import com.devmaster.goatfarm.commercial.api.dto.CommercialSummaryDTO;
import com.devmaster.goatfarm.commercial.api.dto.CustomerRequestDTO;
import com.devmaster.goatfarm.commercial.api.dto.CustomerResponseDTO;
import com.devmaster.goatfarm.commercial.api.dto.MilkSaleRequestDTO;
import com.devmaster.goatfarm.commercial.api.dto.MilkSaleResponseDTO;
import com.devmaster.goatfarm.commercial.api.dto.ReceivableResponseDTO;
import com.devmaster.goatfarm.commercial.api.dto.SalePaymentRequestDTO;
import com.devmaster.goatfarm.commercial.business.bo.AnimalSaleRequestVO;
import com.devmaster.goatfarm.commercial.business.bo.AnimalSaleResponseVO;
import com.devmaster.goatfarm.commercial.business.bo.CommercialSummaryVO;
import com.devmaster.goatfarm.commercial.business.bo.CustomerRequestVO;
import com.devmaster.goatfarm.commercial.business.bo.CustomerResponseVO;
import com.devmaster.goatfarm.commercial.business.bo.MilkSaleRequestVO;
import com.devmaster.goatfarm.commercial.business.bo.MilkSaleResponseVO;
import com.devmaster.goatfarm.commercial.business.bo.ReceivableResponseVO;
import com.devmaster.goatfarm.commercial.business.bo.SalePaymentRequestVO;
import org.springframework.stereotype.Component;

@Component
public class CommercialApiMapper {

    public CustomerRequestVO toVO(CustomerRequestDTO dto) {
        return new CustomerRequestVO(dto.name(), dto.document(), dto.phone(), dto.email(), dto.notes());
    }

    public CustomerResponseDTO toDTO(CustomerResponseVO vo) {
        return new CustomerResponseDTO(vo.id(), vo.name(), vo.document(), vo.phone(), vo.email(), vo.notes(), vo.active());
    }

    public AnimalSaleRequestVO toVO(AnimalSaleRequestDTO dto) {
        return new AnimalSaleRequestVO(dto.goatId(), dto.customerId(), dto.saleDate(), dto.amount(), dto.dueDate(), dto.paymentDate(), dto.notes());
    }

    public AnimalSaleResponseDTO toDTO(AnimalSaleResponseVO vo) {
        return new AnimalSaleResponseDTO(vo.id(), vo.goatRegistrationNumber(), vo.goatName(), vo.customerId(), vo.customerName(), vo.saleDate(), vo.amount(), vo.dueDate(), vo.paymentStatus(), vo.paymentDate(), vo.notes());
    }

    public MilkSaleRequestVO toVO(MilkSaleRequestDTO dto) {
        return new MilkSaleRequestVO(dto.customerId(), dto.saleDate(), dto.quantityLiters(), dto.unitPrice(), dto.dueDate(), dto.paymentDate(), dto.notes());
    }

    public MilkSaleResponseDTO toDTO(MilkSaleResponseVO vo) {
        return new MilkSaleResponseDTO(vo.id(), vo.customerId(), vo.customerName(), vo.saleDate(), vo.quantityLiters(), vo.unitPrice(), vo.totalAmount(), vo.dueDate(), vo.paymentStatus(), vo.paymentDate(), vo.notes());
    }

    public SalePaymentRequestVO toVO(SalePaymentRequestDTO dto) {
        return new SalePaymentRequestVO(dto.paymentDate());
    }

    public ReceivableResponseDTO toDTO(ReceivableResponseVO vo) {
        return new ReceivableResponseDTO(vo.sourceType(), vo.sourceId(), vo.sourceLabel(), vo.customerId(), vo.customerName(), vo.amount(), vo.dueDate(), vo.paymentStatus(), vo.paymentDate(), vo.notes());
    }

    public CommercialSummaryDTO toDTO(CommercialSummaryVO vo) {
        return new CommercialSummaryDTO(vo.customerCount(), vo.animalSalesCount(), vo.animalSalesTotal(), vo.milkSalesCount(), vo.milkSalesQuantityLiters(), vo.milkSalesTotal(), vo.openReceivablesCount(), vo.openReceivablesTotal(), vo.paidReceivablesCount(), vo.paidReceivablesTotal());
    }
}
