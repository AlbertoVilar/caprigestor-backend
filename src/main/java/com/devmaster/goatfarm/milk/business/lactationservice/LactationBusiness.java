package com.devmaster.goatfarm.milk.business.lactationservice;

import com.devmaster.goatfarm.application.ports.in.LactationCommandUseCase;
import com.devmaster.goatfarm.application.ports.in.LactationQueryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LactationBusiness implements LactationCommandUseCase, LactationQueryUseCase {
}
