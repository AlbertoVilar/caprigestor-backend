package com.devmaster.goatfarm.events.converter;

import com.devmaster.goatfarm.events.api.dto.EventRequestDTO;
import com.devmaster.goatfarm.events.api.dto.EventResponseDTO;
import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventDTOConverter {

   public static EventResponseDTO responseDTO(EventResponseVO responseVO) {

       return new EventResponseDTO(
               responseVO.eventId(),
               responseVO.goatId(),
               responseVO.goatName(),
               responseVO.eventType(),
               responseVO.date(),
               responseVO.description(),
               responseVO.location(),
               responseVO.veterinarian(),
               responseVO.outcome()
       );

   }

   public static EventRequestVO toRequestVO(EventRequestDTO requestDTO) {

       return new EventRequestVO(
               requestDTO.goatId(),
               requestDTO.eventType(),
               requestDTO.date(),
               requestDTO.description(),
               requestDTO.location(),
               requestDTO.veterinarian(),
               requestDTO.outcome()
       );
   }

}
