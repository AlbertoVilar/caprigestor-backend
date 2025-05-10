package com.devmaster.goatfarm.tests;

import com.devmaster.goatfarm.events.business.bo.EventRequestVO;
import com.devmaster.goatfarm.events.business.bo.EventResponseVO;
import com.devmaster.goatfarm.events.dao.EventDao;
import com.devmaster.goatfarm.events.enuns.EventType;
import com.devmaster.goatfarm.events.model.entity.Event;
import com.devmaster.goatfarm.events.model.repository.EventRepository;
import com.devmaster.goatfarm.goat.model.entity.Goat;
import com.devmaster.goatfarm.goat.model.repository.GoatRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventDaoTest {

    // Cria um mock do EventRepository para simular o banco de dados de eventos
    @Mock
    private EventRepository eventRepository;

    // Cria um mock do GoatRepository para simular o banco de dados de cabras
    @Mock
    private GoatRepository goatRepository;

    // Injeta os mocks acima dentro de uma instância real de EventDao
    @InjectMocks
    private EventDao eventDao;

    // Inicializa os mocks antes da execução dos testes
    public EventDaoTest() {
        MockitoAnnotations.openMocks(this);
    }

    // Testa se o método createEvent funciona corretamente com dados válidos
    @Test
    public void shouldCreateEventSuccessfully() {
        // Arrange: prepara os dados simulados
        String goatId = "1234567890";

        // Simula uma cabra existente com esse ID
        Goat goat = new Goat();
        goat.setRegistrationNumber(goatId);

        // Cria uma requisição de evento (como se fosse um POST)
        EventRequestVO requestVO = new EventRequestVO(
                goatId,
                EventType.SAUDE,
                LocalDate.now(),
                "Verificação de rotina",
                "Capril Central",
                "Dra. Ana",
                "Tudo normal"
        );

        // Cria um evento simulado como resposta do banco após salvar
        Event savedEvent = new Event(
                1L,
                goat,
                requestVO.eventType(),
                requestVO.date(),
                requestVO.description(),
                requestVO.location(),
                requestVO.veterinarian(),
                requestVO.outcome()
        );

        // Define o comportamento dos mocks
        when(goatRepository.findById(goatId)).thenReturn(Optional.of(goat)); // finge que a cabra existe
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent); // finge que o evento foi salvo

        // Act: executa o método a ser testado
        EventResponseVO response = eventDao.createEvent(requestVO, goatId);

        // Assert: verifica o resultado retornado
        assertNotNull(response); // o retorno não pode ser nulo
        assertEquals("1234567890", response.goatId()); // o ID da cabra deve bater
        assertEquals("Dra. Ana", response.veterinarian()); // o veterinário também

        // Verifica se o método save do repositório foi chamado corretamente
        verify(eventRepository).save(any(Event.class));
    }
}
