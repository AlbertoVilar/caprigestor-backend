package com.devmaster.goatfarm.phone.dao;

import com.devmaster.goatfarm.phone.business.bo.PhoneRequestVO;
import com.devmaster.goatfarm.phone.business.bo.PhoneResponseVO;
import com.devmaster.goatfarm.phone.converter.PhoneEntityConverter;
import com.devmaster.goatfarm.phone.model.entity.Phone;
import com.devmaster.goatfarm.phone.model.repository.PhoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PhoneDAO {

    @Autowired
    private PhoneRepository phoneRepository;

    public PhoneResponseVO createPhone(PhoneRequestVO requestVO) {
        if (requestVO != null) {
            Phone phone = PhoneEntityConverter.toEntity(requestVO);
            phone = phoneRepository.save(phone);
            return PhoneEntityConverter.toVO(phone);
        } else {
            return null;
        }
    }

    public PhoneResponseVO updatePhone(Long id, PhoneRequestVO requestVO) {
        Phone phone = phoneRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Elemento não encontrado " + id));

        PhoneEntityConverter.toUpdateEntity(phone, requestVO);
        return PhoneEntityConverter.toVO(phoneRepository.save(phone));
    }

    public PhoneResponseVO findPhoneById(Long id) {
        Phone phone = phoneRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Elemento não encontrado " + id));
        return PhoneEntityConverter.toVO(phone);
    }

    public List<PhoneResponseVO> findAllPhones() {
        List<Phone> phones = phoneRepository.findAll();
        return phones.stream()
                .map(PhoneEntityConverter::toVO)
                .collect(Collectors.toList());
    }

    public String deletePhone(Long id) {
        if (!phoneRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Elemento não encontrado " + id);
        }
        phoneRepository.deleteById(id);
        return "Telefone com ID " + id + " foi deletado com sucesso.";
    }
}
